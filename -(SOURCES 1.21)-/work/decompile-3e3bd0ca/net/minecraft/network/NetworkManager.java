package net.minecraft.network;

import com.google.common.base.Suppliers;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.handshake.PacketHandshakingInListener;
import net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.login.PacketLoginOutDisconnect;
import net.minecraft.network.protocol.login.PacketLoginOutListener;
import net.minecraft.network.protocol.status.PacketStatusOutListener;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.CancelledPacketHandleException;
import net.minecraft.util.MathHelper;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class NetworkManager extends SimpleChannelInboundHandler<Packet<?>> {

    private static final float AVERAGE_PACKETS_SMOOTHING = 0.75F;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker ROOT_MARKER = MarkerFactory.getMarker("NETWORK");
    public static final Marker PACKET_MARKER = (Marker) SystemUtils.make(MarkerFactory.getMarker("NETWORK_PACKETS"), (marker) -> {
        marker.add(NetworkManager.ROOT_MARKER);
    });
    public static final Marker PACKET_RECEIVED_MARKER = (Marker) SystemUtils.make(MarkerFactory.getMarker("PACKET_RECEIVED"), (marker) -> {
        marker.add(NetworkManager.PACKET_MARKER);
    });
    public static final Marker PACKET_SENT_MARKER = (Marker) SystemUtils.make(MarkerFactory.getMarker("PACKET_SENT"), (marker) -> {
        marker.add(NetworkManager.PACKET_MARKER);
    });
    public static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(() -> {
        return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
    });
    public static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(() -> {
        return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
    });
    public static final Supplier<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(() -> {
        return new DefaultEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
    });
    private static final ProtocolInfo<PacketHandshakingInListener> INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND;
    private final EnumProtocolDirection receiving;
    private volatile boolean sendLoginDisconnect = true;
    private final Queue<Consumer<NetworkManager>> pendingActions = Queues.newConcurrentLinkedQueue();
    public Channel channel;
    public SocketAddress address;
    @Nullable
    private volatile PacketListener disconnectListener;
    @Nullable
    private volatile PacketListener packetListener;
    @Nullable
    private DisconnectionDetails disconnectionDetails;
    private boolean encrypted;
    private boolean disconnectionHandled;
    private int receivedPackets;
    private int sentPackets;
    private float averageReceivedPackets;
    private float averageSentPackets;
    private int tickCount;
    private boolean handlingFault;
    @Nullable
    private volatile DisconnectionDetails delayedDisconnect;
    @Nullable
    BandwidthDebugMonitor bandwidthDebugMonitor;

    public NetworkManager(EnumProtocolDirection enumprotocoldirection) {
        this.receiving = enumprotocoldirection;
    }

    public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
        super.channelActive(channelhandlercontext);
        this.channel = channelhandlercontext.channel();
        this.address = this.channel.remoteAddress();
        if (this.delayedDisconnect != null) {
            this.disconnect(this.delayedDisconnect);
        }

    }

    public void channelInactive(ChannelHandlerContext channelhandlercontext) {
        this.disconnect((IChatBaseComponent) IChatBaseComponent.translatable("disconnect.endOfStream"));
    }

    public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {
        if (throwable instanceof SkipEncodeException) {
            NetworkManager.LOGGER.debug("Skipping packet due to errors", throwable.getCause());
        } else {
            boolean flag = !this.handlingFault;

            this.handlingFault = true;
            if (this.channel.isOpen()) {
                if (throwable instanceof TimeoutException) {
                    NetworkManager.LOGGER.debug("Timeout", throwable);
                    this.disconnect((IChatBaseComponent) IChatBaseComponent.translatable("disconnect.timeout"));
                } else {
                    IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable("disconnect.genericReason", "Internal Exception: " + String.valueOf(throwable));
                    PacketListener packetlistener = this.packetListener;
                    DisconnectionDetails disconnectiondetails;

                    if (packetlistener != null) {
                        disconnectiondetails = packetlistener.createDisconnectionInfo(ichatmutablecomponent, throwable);
                    } else {
                        disconnectiondetails = new DisconnectionDetails(ichatmutablecomponent);
                    }

                    if (flag) {
                        NetworkManager.LOGGER.debug("Failed to sent packet", throwable);
                        if (this.getSending() == EnumProtocolDirection.CLIENTBOUND) {
                            Packet<?> packet = this.sendLoginDisconnect ? new PacketLoginOutDisconnect(ichatmutablecomponent) : new ClientboundDisconnectPacket(ichatmutablecomponent);

                            this.send((Packet) packet, PacketSendListener.thenRun(() -> {
                                this.disconnect(disconnectiondetails);
                            }));
                        } else {
                            this.disconnect(disconnectiondetails);
                        }

                        this.setReadOnly();
                    } else {
                        NetworkManager.LOGGER.debug("Double fault", throwable);
                        this.disconnect(disconnectiondetails);
                    }
                }

            }
        }
    }

    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet<?> packet) {
        if (this.channel.isOpen()) {
            PacketListener packetlistener = this.packetListener;

            if (packetlistener == null) {
                throw new IllegalStateException("Received a packet before the packet listener was initialized");
            } else {
                if (packetlistener.shouldHandleMessage(packet)) {
                    try {
                        genericsFtw(packet, packetlistener);
                    } catch (CancelledPacketHandleException cancelledpackethandleexception) {
                        ;
                    } catch (RejectedExecutionException rejectedexecutionexception) {
                        this.disconnect((IChatBaseComponent) IChatBaseComponent.translatable("multiplayer.disconnect.server_shutdown"));
                    } catch (ClassCastException classcastexception) {
                        NetworkManager.LOGGER.error("Received {} that couldn't be processed", packet.getClass(), classcastexception);
                        this.disconnect((IChatBaseComponent) IChatBaseComponent.translatable("multiplayer.disconnect.invalid_packet"));
                    }

                    ++this.receivedPackets;
                }

            }
        }
    }

    private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener packetlistener) {
        packet.handle(packetlistener);
    }

    private void validateListener(ProtocolInfo<?> protocolinfo, PacketListener packetlistener) {
        Validate.notNull(packetlistener, "packetListener", new Object[0]);
        EnumProtocolDirection enumprotocoldirection = packetlistener.flow();
        String s;

        if (enumprotocoldirection != this.receiving) {
            s = String.valueOf(this.receiving);
            throw new IllegalStateException("Trying to set listener for wrong side: connection is " + s + ", but listener is " + String.valueOf(enumprotocoldirection));
        } else {
            EnumProtocol enumprotocol = packetlistener.protocol();

            if (protocolinfo.id() != enumprotocol) {
                s = String.valueOf(enumprotocol);
                throw new IllegalStateException("Listener protocol (" + s + ") does not match requested one " + String.valueOf(protocolinfo));
            }
        }
    }

    private static void syncAfterConfigurationChange(ChannelFuture channelfuture) {
        try {
            channelfuture.syncUninterruptibly();
        } catch (Exception exception) {
            if (exception instanceof ClosedChannelException) {
                NetworkManager.LOGGER.info("Connection closed during protocol change");
            } else {
                throw exception;
            }
        }
    }

    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocolinfo, T t0) {
        this.validateListener(protocolinfo, t0);
        if (protocolinfo.flow() != this.getReceiving()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf(protocolinfo.id()));
        } else {
            this.packetListener = t0;
            this.disconnectListener = null;
            UnconfiguredPipelineHandler.b unconfiguredpipelinehandler_b = UnconfiguredPipelineHandler.setupInboundProtocol(protocolinfo);
            BundlerInfo bundlerinfo = protocolinfo.bundlerInfo();

            if (bundlerinfo != null) {
                PacketBundlePacker packetbundlepacker = new PacketBundlePacker(bundlerinfo);

                unconfiguredpipelinehandler_b = unconfiguredpipelinehandler_b.andThen((channelhandlercontext) -> {
                    channelhandlercontext.pipeline().addAfter("decoder", "bundler", packetbundlepacker);
                });
            }

            syncAfterConfigurationChange(this.channel.writeAndFlush(unconfiguredpipelinehandler_b));
        }
    }

    public void setupOutboundProtocol(ProtocolInfo<?> protocolinfo) {
        if (protocolinfo.flow() != this.getSending()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf(protocolinfo.id()));
        } else {
            UnconfiguredPipelineHandler.d unconfiguredpipelinehandler_d = UnconfiguredPipelineHandler.setupOutboundProtocol(protocolinfo);
            BundlerInfo bundlerinfo = protocolinfo.bundlerInfo();

            if (bundlerinfo != null) {
                PacketBundleUnpacker packetbundleunpacker = new PacketBundleUnpacker(bundlerinfo);

                unconfiguredpipelinehandler_d = unconfiguredpipelinehandler_d.andThen((channelhandlercontext) -> {
                    channelhandlercontext.pipeline().addAfter("encoder", "unbundler", packetbundleunpacker);
                });
            }

            boolean flag = protocolinfo.id() == EnumProtocol.LOGIN;

            syncAfterConfigurationChange(this.channel.writeAndFlush(unconfiguredpipelinehandler_d.andThen((channelhandlercontext) -> {
                this.sendLoginDisconnect = flag;
            })));
        }
    }

    public void setListenerForServerboundHandshake(PacketListener packetlistener) {
        if (this.packetListener != null) {
            throw new IllegalStateException("Listener already set");
        } else if (this.receiving == EnumProtocolDirection.SERVERBOUND && packetlistener.flow() == EnumProtocolDirection.SERVERBOUND && packetlistener.protocol() == NetworkManager.INITIAL_PROTOCOL.id()) {
            this.packetListener = packetlistener;
        } else {
            throw new IllegalStateException("Invalid initial listener");
        }
    }

    public void initiateServerboundStatusConnection(String s, int i, PacketStatusOutListener packetstatusoutlistener) {
        this.initiateServerboundConnection(s, i, StatusProtocols.SERVERBOUND, StatusProtocols.CLIENTBOUND, packetstatusoutlistener, ClientIntent.STATUS);
    }

    public void initiateServerboundPlayConnection(String s, int i, PacketLoginOutListener packetloginoutlistener) {
        this.initiateServerboundConnection(s, i, LoginProtocols.SERVERBOUND, LoginProtocols.CLIENTBOUND, packetloginoutlistener, ClientIntent.LOGIN);
    }

    public <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundPlayConnection(String s, int i, ProtocolInfo<S> protocolinfo, ProtocolInfo<C> protocolinfo1, C c0, boolean flag) {
        this.initiateServerboundConnection(s, i, protocolinfo, protocolinfo1, c0, flag ? ClientIntent.TRANSFER : ClientIntent.LOGIN);
    }

    private <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundConnection(String s, int i, ProtocolInfo<S> protocolinfo, ProtocolInfo<C> protocolinfo1, C c0, ClientIntent clientintent) {
        if (protocolinfo.id() != protocolinfo1.id()) {
            throw new IllegalStateException("Mismatched initial protocols");
        } else {
            this.disconnectListener = c0;
            this.runOnceConnected((networkmanager) -> {
                this.setupInboundProtocol(protocolinfo1, c0);
                networkmanager.sendPacket(new PacketHandshakingInSetProtocol(SharedConstants.getCurrentVersion().getProtocolVersion(), s, i, clientintent), (PacketSendListener) null, true);
                this.setupOutboundProtocol(protocolinfo);
            });
        }
    }

    public void send(Packet<?> packet) {
        this.send(packet, (PacketSendListener) null);
    }

    public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {
        this.send(packet, packetsendlistener, true);
    }

    public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener, boolean flag) {
        if (this.isConnected()) {
            this.flushQueue();
            this.sendPacket(packet, packetsendlistener, flag);
        } else {
            this.pendingActions.add((networkmanager) -> {
                networkmanager.sendPacket(packet, packetsendlistener, flag);
            });
        }

    }

    public void runOnceConnected(Consumer<NetworkManager> consumer) {
        if (this.isConnected()) {
            this.flushQueue();
            consumer.accept(this);
        } else {
            this.pendingActions.add(consumer);
        }

    }

    private void sendPacket(Packet<?> packet, @Nullable PacketSendListener packetsendlistener, boolean flag) {
        ++this.sentPackets;
        if (this.channel.eventLoop().inEventLoop()) {
            this.doSendPacket(packet, packetsendlistener, flag);
        } else {
            this.channel.eventLoop().execute(() -> {
                this.doSendPacket(packet, packetsendlistener, flag);
            });
        }

    }

    private void doSendPacket(Packet<?> packet, @Nullable PacketSendListener packetsendlistener, boolean flag) {
        ChannelFuture channelfuture = flag ? this.channel.writeAndFlush(packet) : this.channel.write(packet);

        if (packetsendlistener != null) {
            channelfuture.addListener((future) -> {
                if (future.isSuccess()) {
                    packetsendlistener.onSuccess();
                } else {
                    Packet<?> packet1 = packetsendlistener.onFailure();

                    if (packet1 != null) {
                        ChannelFuture channelfuture1 = this.channel.writeAndFlush(packet1);

                        channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                }

            });
        }

        channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void flushChannel() {
        if (this.isConnected()) {
            this.flush();
        } else {
            this.pendingActions.add(NetworkManager::flush);
        }

    }

    private void flush() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> {
                this.channel.flush();
            });
        }

    }

    private void flushQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            Queue queue = this.pendingActions;

            synchronized (this.pendingActions) {
                Consumer consumer;

                while ((consumer = (Consumer) this.pendingActions.poll()) != null) {
                    consumer.accept(this);
                }

            }
        }
    }

    public void tick() {
        this.flushQueue();
        PacketListener packetlistener = this.packetListener;

        if (packetlistener instanceof TickablePacketListener tickablepacketlistener) {
            tickablepacketlistener.tick();
        }

        if (!this.isConnected() && !this.disconnectionHandled) {
            this.handleDisconnection();
        }

        if (this.channel != null) {
            this.channel.flush();
        }

        if (this.tickCount++ % 20 == 0) {
            this.tickSecond();
        }

        if (this.bandwidthDebugMonitor != null) {
            this.bandwidthDebugMonitor.tick();
        }

    }

    protected void tickSecond() {
        this.averageSentPackets = MathHelper.lerp(0.75F, (float) this.sentPackets, this.averageSentPackets);
        this.averageReceivedPackets = MathHelper.lerp(0.75F, (float) this.receivedPackets, this.averageReceivedPackets);
        this.sentPackets = 0;
        this.receivedPackets = 0;
    }

    public SocketAddress getRemoteAddress() {
        return this.address;
    }

    public String getLoggableAddress(boolean flag) {
        return this.address == null ? "local" : (flag ? this.address.toString() : "IP hidden");
    }

    public void disconnect(IChatBaseComponent ichatbasecomponent) {
        this.disconnect(new DisconnectionDetails(ichatbasecomponent));
    }

    public void disconnect(DisconnectionDetails disconnectiondetails) {
        if (this.channel == null) {
            this.delayedDisconnect = disconnectiondetails;
        }

        if (this.isConnected()) {
            this.channel.close().awaitUninterruptibly();
            this.disconnectionDetails = disconnectiondetails;
        }

    }

    public boolean isMemoryConnection() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public EnumProtocolDirection getReceiving() {
        return this.receiving;
    }

    public EnumProtocolDirection getSending() {
        return this.receiving.getOpposite();
    }

    public static NetworkManager connectToServer(InetSocketAddress inetsocketaddress, boolean flag, @Nullable LocalSampleLogger localsamplelogger) {
        NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.CLIENTBOUND);

        if (localsamplelogger != null) {
            networkmanager.setBandwidthLogger(localsamplelogger);
        }

        ChannelFuture channelfuture = connect(inetsocketaddress, flag, networkmanager);

        channelfuture.syncUninterruptibly();
        return networkmanager;
    }

    public static ChannelFuture connect(InetSocketAddress inetsocketaddress, boolean flag, final NetworkManager networkmanager) {
        Class oclass;
        EventLoopGroup eventloopgroup;

        if (Epoll.isAvailable() && flag) {
            oclass = EpollSocketChannel.class;
            eventloopgroup = (EventLoopGroup) NetworkManager.NETWORK_EPOLL_WORKER_GROUP.get();
        } else {
            oclass = NioSocketChannel.class;
            eventloopgroup = (EventLoopGroup) NetworkManager.NETWORK_WORKER_GROUP.get();
        }

        return ((Bootstrap) ((Bootstrap) ((Bootstrap) (new Bootstrap()).group(eventloopgroup)).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelexception) {
                    ;
                }

                ChannelPipeline channelpipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));

                NetworkManager.configureSerialization(channelpipeline, EnumProtocolDirection.CLIENTBOUND, false, networkmanager.bandwidthDebugMonitor);
                networkmanager.configurePacketHandler(channelpipeline);
            }
        })).channel(oclass)).connect(inetsocketaddress.getAddress(), inetsocketaddress.getPort());
    }

    private static String outboundHandlerName(boolean flag) {
        return flag ? "encoder" : "outbound_config";
    }

    private static String inboundHandlerName(boolean flag) {
        return flag ? "decoder" : "inbound_config";
    }

    public void configurePacketHandler(ChannelPipeline channelpipeline) {
        channelpipeline.addLast("hackfix", new ChannelOutboundHandlerAdapter(this) {
            public void write(ChannelHandlerContext channelhandlercontext, Object object, ChannelPromise channelpromise) throws Exception {
                super.write(channelhandlercontext, object, channelpromise);
            }
        }).addLast("packet_handler", this);
    }

    public static void configureSerialization(ChannelPipeline channelpipeline, EnumProtocolDirection enumprotocoldirection, boolean flag, @Nullable BandwidthDebugMonitor bandwidthdebugmonitor) {
        EnumProtocolDirection enumprotocoldirection1 = enumprotocoldirection.getOpposite();
        boolean flag1 = enumprotocoldirection == EnumProtocolDirection.SERVERBOUND;
        boolean flag2 = enumprotocoldirection1 == EnumProtocolDirection.SERVERBOUND;

        channelpipeline.addLast("splitter", createFrameDecoder(bandwidthdebugmonitor, flag)).addLast(new ChannelHandler[]{new FlowControlHandler()}).addLast(inboundHandlerName(flag1), (ChannelHandler) (flag1 ? new PacketDecoder<>(NetworkManager.INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.a())).addLast("prepender", createFrameEncoder(flag)).addLast(outboundHandlerName(flag2), (ChannelHandler) (flag2 ? new PacketEncoder<>(NetworkManager.INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.c()));
    }

    private static ChannelOutboundHandler createFrameEncoder(boolean flag) {
        return (ChannelOutboundHandler) (flag ? new NoOpFrameEncoder() : new PacketPrepender());
    }

    private static ChannelInboundHandler createFrameDecoder(@Nullable BandwidthDebugMonitor bandwidthdebugmonitor, boolean flag) {
        return (ChannelInboundHandler) (!flag ? new PacketSplitter(bandwidthdebugmonitor) : (bandwidthdebugmonitor != null ? new MonitorFrameDecoder(bandwidthdebugmonitor) : new NoOpFrameDecoder()));
    }

    public static void configureInMemoryPipeline(ChannelPipeline channelpipeline, EnumProtocolDirection enumprotocoldirection) {
        configureSerialization(channelpipeline, enumprotocoldirection, true, (BandwidthDebugMonitor) null);
    }

    public static NetworkManager connectToLocalServer(SocketAddress socketaddress) {
        final NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.CLIENTBOUND);

        ((Bootstrap) ((Bootstrap) ((Bootstrap) (new Bootstrap()).group((EventLoopGroup) NetworkManager.LOCAL_WORKER_GROUP.get())).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                ChannelPipeline channelpipeline = channel.pipeline();

                NetworkManager.configureInMemoryPipeline(channelpipeline, EnumProtocolDirection.CLIENTBOUND);
                networkmanager.configurePacketHandler(channelpipeline);
            }
        })).channel(LocalChannel.class)).connect(socketaddress).syncUninterruptibly();
        return networkmanager;
    }

    public void setEncryptionKey(Cipher cipher, Cipher cipher1) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new PacketDecrypter(cipher));
        this.channel.pipeline().addBefore("prepender", "encrypt", new PacketEncrypter(cipher1));
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isConnecting() {
        return this.channel == null;
    }

    @Nullable
    public PacketListener getPacketListener() {
        return this.packetListener;
    }

    @Nullable
    public DisconnectionDetails getDisconnectionDetails() {
        return this.disconnectionDetails;
    }

    public void setReadOnly() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }

    }

    public void setupCompression(int i, boolean flag) {
        if (i >= 0) {
            ChannelHandler channelhandler = this.channel.pipeline().get("decompress");

            if (channelhandler instanceof PacketDecompressor) {
                PacketDecompressor packetdecompressor = (PacketDecompressor) channelhandler;

                packetdecompressor.setThreshold(i, flag);
            } else {
                this.channel.pipeline().addAfter("splitter", "decompress", new PacketDecompressor(i, flag));
            }

            channelhandler = this.channel.pipeline().get("compress");
            if (channelhandler instanceof PacketCompressor) {
                PacketCompressor packetcompressor = (PacketCompressor) channelhandler;

                packetcompressor.setThreshold(i);
            } else {
                this.channel.pipeline().addAfter("prepender", "compress", new PacketCompressor(i));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof PacketDecompressor) {
                this.channel.pipeline().remove("decompress");
            }

            if (this.channel.pipeline().get("compress") instanceof PacketCompressor) {
                this.channel.pipeline().remove("compress");
            }
        }

    }

    public void handleDisconnection() {
        if (this.channel != null && !this.channel.isOpen()) {
            if (this.disconnectionHandled) {
                NetworkManager.LOGGER.warn("handleDisconnection() called twice");
            } else {
                this.disconnectionHandled = true;
                PacketListener packetlistener = this.getPacketListener();
                PacketListener packetlistener1 = packetlistener != null ? packetlistener : this.disconnectListener;

                if (packetlistener1 != null) {
                    DisconnectionDetails disconnectiondetails = (DisconnectionDetails) Objects.requireNonNullElseGet(this.getDisconnectionDetails(), () -> {
                        return new DisconnectionDetails(IChatBaseComponent.translatable("multiplayer.disconnect.generic"));
                    });

                    packetlistener1.onDisconnect(disconnectiondetails);
                }

            }
        }
    }

    public float getAverageReceivedPackets() {
        return this.averageReceivedPackets;
    }

    public float getAverageSentPackets() {
        return this.averageSentPackets;
    }

    public void setBandwidthLogger(LocalSampleLogger localsamplelogger) {
        this.bandwidthDebugMonitor = new BandwidthDebugMonitor(localsamplelogger);
    }
}
