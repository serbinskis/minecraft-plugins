package net.minecraft.server.network;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.PacketStatusInListener;
import net.minecraft.network.protocol.status.PacketStatusInStart;
import net.minecraft.network.protocol.status.PacketStatusOutServerInfo;
import net.minecraft.network.protocol.status.ServerPing;

public class PacketStatusListener implements PacketStatusInListener {

    private static final IChatBaseComponent DISCONNECT_REASON = IChatBaseComponent.translatable("multiplayer.status.request_handled");
    private final ServerPing status;
    private final NetworkManager connection;
    private boolean hasRequestedStatus;

    public PacketStatusListener(ServerPing serverping, NetworkManager networkmanager) {
        this.status = serverping;
        this.connection = networkmanager;
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectiondetails) {}

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    public void handleStatusRequest(PacketStatusInStart packetstatusinstart) {
        if (this.hasRequestedStatus) {
            this.connection.disconnect(PacketStatusListener.DISCONNECT_REASON);
        } else {
            this.hasRequestedStatus = true;
            this.connection.send(new PacketStatusOutServerInfo(this.status));
        }
    }

    @Override
    public void handlePingRequest(ServerboundPingRequestPacket serverboundpingrequestpacket) {
        this.connection.send(new ClientboundPongResponsePacket(serverboundpingrequestpacket.getTime()));
        this.connection.disconnect(PacketStatusListener.DISCONNECT_REASON);
    }
}
