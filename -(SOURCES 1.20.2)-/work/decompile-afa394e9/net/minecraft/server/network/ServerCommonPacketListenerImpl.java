package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.SystemUtils;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PlayerConnectionUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.thread.IAsyncTaskHandler;
import org.slf4j.Logger;

public abstract class ServerCommonPacketListenerImpl implements ServerCommonPacketListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int LATENCY_CHECK_INTERVAL = 15000;
    private static final IChatBaseComponent TIMEOUT_DISCONNECTION_MESSAGE = IChatBaseComponent.translatable("disconnect.timeout");
    protected final MinecraftServer server;
    protected final NetworkManager connection;
    private long keepAliveTime;
    private boolean keepAlivePending;
    private long keepAliveChallenge;
    private int latency;
    private volatile boolean suspendFlushingOnServerThread = false;

    public ServerCommonPacketListenerImpl(MinecraftServer minecraftserver, NetworkManager networkmanager, CommonListenerCookie commonlistenercookie) {
        this.server = minecraftserver;
        this.connection = networkmanager;
        this.keepAliveTime = SystemUtils.getMillis();
        this.latency = commonlistenercookie.latency();
    }

    @Override
    public void onDisconnect(IChatBaseComponent ichatbasecomponent) {
        if (this.isSingleplayerOwner()) {
            ServerCommonPacketListenerImpl.LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.halt(false);
        }

    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket serverboundkeepalivepacket) {
        if (this.keepAlivePending && serverboundkeepalivepacket.getId() == this.keepAliveChallenge) {
            int i = (int) (SystemUtils.getMillis() - this.keepAliveTime);

            this.latency = (this.latency * 3 + i) / 4;
            this.keepAlivePending = false;
        } else if (!this.isSingleplayerOwner()) {
            this.disconnect(ServerCommonPacketListenerImpl.TIMEOUT_DISCONNECTION_MESSAGE);
        }

    }

    @Override
    public void handlePong(ServerboundPongPacket serverboundpongpacket) {}

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundcustompayloadpacket) {}

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundresourcepackpacket) {
        PlayerConnectionUtils.ensureRunningOnSameThread(serverboundresourcepackpacket, this, (IAsyncTaskHandler) this.server);
        if (serverboundresourcepackpacket.getAction() == ServerboundResourcePackPacket.a.DECLINED && this.server.isResourcePackRequired()) {
            ServerCommonPacketListenerImpl.LOGGER.info("Disconnecting {} due to resource pack rejection", this.playerProfile().getName());
            this.disconnect(IChatBaseComponent.translatable("multiplayer.requiredTexturePrompt.disconnect"));
        }

    }

    protected void keepConnectionAlive() {
        this.server.getProfiler().push("keepAlive");
        long i = SystemUtils.getMillis();

        if (i - this.keepAliveTime >= 15000L) {
            if (this.keepAlivePending) {
                this.disconnect(ServerCommonPacketListenerImpl.TIMEOUT_DISCONNECTION_MESSAGE);
            } else {
                this.keepAlivePending = true;
                this.keepAliveTime = i;
                this.keepAliveChallenge = i;
                this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
            }
        }

        this.server.getProfiler().pop();
    }

    public void suspendFlushing() {
        this.suspendFlushingOnServerThread = true;
    }

    public void resumeFlushing() {
        this.suspendFlushingOnServerThread = false;
        this.connection.flushChannel();
    }

    public void send(Packet<?> packet) {
        this.send(packet, (PacketSendListener) null);
    }

    public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {
        boolean flag = !this.suspendFlushingOnServerThread || !this.server.isSameThread();

        try {
            this.connection.send(packet, packetsendlistener, flag);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Sending packet");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Packet being sent");

            crashreportsystemdetails.setDetail("Packet class", () -> {
                return packet.getClass().getCanonicalName();
            });
            throw new ReportedException(crashreport);
        }
    }

    public void disconnect(IChatBaseComponent ichatbasecomponent) {
        this.connection.send(new ClientboundDisconnectPacket(ichatbasecomponent), PacketSendListener.thenRun(() -> {
            this.connection.disconnect(ichatbasecomponent);
        }));
        this.connection.setReadOnly();
        MinecraftServer minecraftserver = this.server;
        NetworkManager networkmanager = this.connection;

        Objects.requireNonNull(this.connection);
        minecraftserver.executeBlocking(networkmanager::handleDisconnection);
    }

    protected boolean isSingleplayerOwner() {
        return this.server.isSingleplayerOwner(this.playerProfile());
    }

    protected abstract GameProfile playerProfile();

    @VisibleForDebug
    public GameProfile getOwner() {
        return this.playerProfile();
    }

    public int latency() {
        return this.latency;
    }

    protected CommonListenerCookie createCookie(ClientInformation clientinformation) {
        return new CommonListenerCookie(this.playerProfile(), this.latency, clientinformation);
    }
}
