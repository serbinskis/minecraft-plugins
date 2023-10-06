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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
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
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol;
import net.minecraft.network.protocol.login.PacketLoginOutDisconnect;
import net.minecraft.network.protocol.login.PacketLoginOutListener;
import net.minecraft.network.protocol.status.PacketStatusOutListener;
import net.minecraft.server.CancelledPacketHandleException;
import net.minecraft.util.MathHelper;
import net.minecraft.util.SampleLogger;
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
    public static final AttributeKey<EnumProtocol.a<?>> ATTRIBUTE_SERVERBOUND_PROTOCOL = AttributeKey.valueOf("serverbound_protocol");
    public static final AttributeKey<EnumProtocol.a<?>> ATTRIBUTE_CLIENTBOUND_PROTOCOL = AttributeKey.valueOf("clientbound_protocol");
    public static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(() -> {
        return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
    });
    public static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(() -> {
        return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
    });
    public static final Supplier<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(() -> {
        return new DefaultEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
    });
    private final EnumProtocolDirection receiving;
    private final Queue<Consumer<NetworkManager>> pendingActions = Queues.newConcurrentLinkedQueue();
    public Channel channel;
    public SocketAddress address;
    @Nullable
    private volatile PacketListener disconnectListener;
    @Nullable
    private volatile PacketListener packetListener;
    @Nullable
    private IChatBaseComponent disconnectedReason;
    private boolean encrypted;
    private boolean disconnectionHandled;
    private int receivedPackets;
    private int sentPackets;
    private float averageReceivedPackets;
    private float averageSentPackets;
    private int tickCount;
    private boolean handlingFault;
    @Nullable
    private volatile IChatBaseComponent delayedDisconnect;
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

    public static void setInitialProtocolAttributes(Channel channel) {
        channel.attr(NetworkManager.ATTRIBUTE_SERVERBOUND_PROTOCOL).set(EnumProtocol.HANDSHAKING.codec(EnumProtocolDirection.SERVERBOUND));
        channel.attr(NetworkManager.ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(EnumProtocol.HANDSHAKING.codec(EnumProtocolDirection.CLIENTBOUND));
    }

    public void channelInactive(ChannelHandlerContext channelhandlercontext) {
        this.disconnect(IChatBaseComponent.translatable("disconnect.endOfStream"));
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
                    this.disconnect(IChatBaseComponent.translatable("disconnect.timeout"));
                } else {
                    IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable("disconnect.genericReason", "Internal Exception: " + throwable);

                    if (flag) {
                        NetworkManager.LOGGER.debug("Failed to sent packet", throwable);
                        if (this.getSending() == EnumProtocolDirection.CLIENTBOUND) {
                            EnumProtocol enumprotocol = ((EnumProtocol.a) this.channel.attr(NetworkManager.ATTRIBUTE_CLIENTBOUND_PROTOCOL).get()).protocol();
                            Packet<?> packet = enumprotocol == EnumProtocol.LOGIN ? new PacketLoginOutDisconnect(ichatmutablecomponent) : new ClientboundDisconnectPacket(ichatmutablecomponent);

                            this.send((Packet) packet, PacketSendListener.thenRun(() -> {
                                this.disconnect(ichatmutablecomponent);
                            }));
                        } else {
                            this.disconnect(ichatmutablecomponent);
                        }

                        this.setReadOnly();
                    } else {
                        NetworkManager.LOGGER.debug("Double fault", throwable);
                        this.disconnect(ichatmutablecomponent);
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
                        this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.server_shutdown"));
                    } catch (ClassCastException classcastexception) {
                        NetworkManager.LOGGER.error("Received {} that couldn't be processed", packet.getClass(), classcastexception);
                        this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.invalid_packet"));
                    }

                    ++this.receivedPackets;
                }

            }
        }
    }

    private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener packetlistener) {
        packet.handle(packetlistener);
    }

    public void suspendInboundAfterProtocolChange() {
        this.channel.config().setAutoRead(false);
    }

    public void resumeInboundAfterProtocolChange() {
        this.channel.config().setAutoRead(true);
    }

    public void setListener(PacketListener packetlistener) {
        Validate.notNull(packetlistener, "packetListener", new Object[0]);
        EnumProtocolDirection enumprotocoldirection = packetlistener.flow();

        if (enumprotocoldirection != this.receiving) {
            throw new IllegalStateException("Trying to set listener for wrong side: connection is " + this.receiving + ", but listener is " + enumprotocoldirection);
        } else {
            EnumProtocol enumprotocol = packetlistener.protocol();
            EnumProtocol enumprotocol1 = ((EnumProtocol.a) this.channel.attr(getProtocolKey(enumprotocoldirection)).get()).protocol();

            if (enumprotocol1 != enumprotocol) {
                throw new IllegalStateException("Trying to set listener for protocol " + enumprotocol.id() + ", but current " + enumprotocoldirection + " protocol is " + enumprotocol1.id());
            } else {
                this.packetListener = packetlistener;
                this.disconnectListener = null;
            }
        }
    }

    public void setListenerForServerboundHandshake(PacketListener packetlistener) {
        if (this.packetListener != null) {
            throw new IllegalStateException("Listener already set");
        } else if (this.receiving == EnumProtocolDirection.SERVERBOUND && packetlistener.flow() == EnumProtocolDirection.SERVERBOUND && packetlistener.protocol() == EnumProtocol.HANDSHAKING) {
            this.packetListener = packetlistener;
        } else {
            throw new IllegalStateException("Invalid initial listener");
        }
    }

    public void initiateServerboundStatusConnection(String s, int i, PacketStatusOutListener packetstatusoutlistener) {
        this.initiateServerboundConnection(s, i, packetstatusoutlistener, ClientIntent.STATUS);
    }

    public void initiateServerboundPlayConnection(String s, int i, PacketLoginOutListener packetloginoutlistener) {
        this.initiateServerboundConnection(s, i, packetloginoutlistener, ClientIntent.LOGIN);
    }

    private void initiateServerboundConnection(String s, int i, PacketListener packetlistener, ClientIntent clientintent) {
        this.disconnectListener = packetlistener;
        this.runOnceConnected((networkmanager) -> {
            networkmanager.setClientboundProtocolAfterHandshake(clientintent);
            this.setListener(packetlistener);
            networkmanager.sendPacket(new PacketHandshakingInSetProtocol(SharedConstants.getCurrentVersion().getProtocolVersion(), s, i, clientintent), (PacketSendListener) null, true);
        });
    }

    public void setClientboundProtocolAfterHandshake(ClientIntent clientintent) {
        this.channel.attr(NetworkManager.ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(clientintent.protocol().codec(EnumProtocolDirection.CLIENTBOUND));
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

    private static AttributeKey<EnumProtocol.a<?>> getProtocolKey(EnumProtocolDirection enumprotocoldirection) {
        AttributeKey attributekey;

        switch (enumprotocoldirection) {
            case CLIENTBOUND:
                attributekey = NetworkManager.ATTRIBUTE_CLIENTBOUND_PROTOCOL;
                break;
            case SERVERBOUND:
                attributekey = NetworkManager.ATTRIBUTE_SERVERBOUND_PROTOCOL;
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return attributekey;
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

        if (packetlistener instanceof TickablePacketListener) {
            TickablePacketListener tickablepacketlistener = (TickablePacketListener) packetlistener;

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
        if (this.channel == null) {
            this.delayedDisconnect = ichatbasecomponent;
        }

        if (this.isConnected()) {
            this.channel.close().awaitUninterruptibly();
            this.disconnectedReason = ichatbasecomponent;
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

    public static NetworkManager connectToServer(InetSocketAddress inetsocketaddress, boolean flag, @Nullable SampleLogger samplelogger) {
        NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.CLIENTBOUND);

        if (samplelogger != null) {
            networkmanager.setBandwidthLogger(samplelogger);
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
                NetworkManager.setInitialProtocolAttributes(channel);

                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelexception) {
                    ;
                }

                ChannelPipeline channelpipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));

                NetworkManager.configureSerialization(channelpipeline, EnumProtocolDirection.CLIENTBOUND, networkmanager.bandwidthDebugMonitor);
                networkmanager.configurePacketHandler(channelpipeline);
            }
        })).channel(oclass)).connect(inetsocketaddress.getAddress(), inetsocketaddress.getPort());
    }

    public static void configureSerialization(ChannelPipeline channelpipeline, EnumProtocolDirection enumprotocoldirection, @Nullable BandwidthDebugMonitor bandwidthdebugmonitor) {
        EnumProtocolDirection enumprotocoldirection1 = enumprotocoldirection.getOpposite();
        AttributeKey<EnumProtocol.a<?>> attributekey = getProtocolKey(enumprotocoldirection);
        AttributeKey<EnumProtocol.a<?>> attributekey1 = getProtocolKey(enumprotocoldirection1);

        channelpipeline.addLast("splitter", new PacketSplitter(bandwidthdebugmonitor)).addLast("decoder", new PacketDecoder(attributekey)).addLast("prepender", new PacketPrepender()).addLast("encoder", new PacketEncoder(attributekey1)).addLast("unbundler", new PacketBundleUnpacker(attributekey1)).addLast("bundler", new PacketBundlePacker(attributekey));
    }

    public void configurePacketHandler(ChannelPipeline channelpipeline) {
        channelpipeline.addLast(new ChannelHandler[]{new FlowControlHandler()}).addLast("packet_handler", this);
    }

    private static void configureInMemoryPacketValidation(ChannelPipeline channelpipeline, EnumProtocolDirection enumprotocoldirection) {
        EnumProtocolDirection enumprotocoldirection1 = enumprotocoldirection.getOpposite();
        AttributeKey<EnumProtocol.a<?>> attributekey = getProtocolKey(enumprotocoldirection);
        AttributeKey<EnumProtocol.a<?>> attributekey1 = getProtocolKey(enumprotocoldirection1);

        channelpipeline.addLast("validator", new PacketFlowValidator(attributekey, attributekey1));
    }

    public static void configureInMemoryPipeline(ChannelPipeline channelpipeline, EnumProtocolDirection enumprotocoldirection) {
        configureInMemoryPacketValidation(channelpipeline, enumprotocoldirection);
    }

    public static NetworkManager connectToLocalServer(SocketAddress socketaddress) {
        final NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.CLIENTBOUND);

        ((Bootstrap) ((Bootstrap) ((Bootstrap) (new Bootstrap()).group((EventLoopGroup) NetworkManager.LOCAL_WORKER_GROUP.get())).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                NetworkManager.setInitialProtocolAttributes(channel);
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
    public IChatBaseComponent getDisconnectedReason() {
        return this.disconnectedReason;
    }

    public void setReadOnly() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }

    }

    public void setupCompression(int i, boolean flag) {
        if (i >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof PacketDecompressor) {
                ((PacketDecompressor) this.channel.pipeline().get("decompress")).setThreshold(i, flag);
            } else {
                this.channel.pipeline().addBefore("decoder", "decompress", new PacketDecompressor(i, flag));
            }

            if (this.channel.pipeline().get("compress") instanceof PacketCompressor) {
                ((PacketCompressor) this.channel.pipeline().get("compress")).setThreshold(i);
            } else {
                this.channel.pipeline().addBefore("encoder", "compress", new PacketCompressor(i));
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
                    IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) Objects.requireNonNullElseGet(this.getDisconnectedReason(), () -> {
                        return IChatBaseComponent.translatable("multiplayer.disconnect.generic");
                    });

                    packetlistener1.onDisconnect(ichatbasecomponent);
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

    public void setBandwidthLogger(SampleLogger samplelogger) {
        this.bandwidthDebugMonitor = new BandwidthDebugMonitor(samplelogger);
    }
}
