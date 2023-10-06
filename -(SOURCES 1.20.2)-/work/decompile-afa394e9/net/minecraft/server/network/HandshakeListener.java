package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.PacketHandshakingInListener;
import net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol;
import net.minecraft.network.protocol.login.PacketLoginOutDisconnect;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.server.MinecraftServer;

public class HandshakeListener implements PacketHandshakingInListener {

    private static final IChatBaseComponent IGNORE_STATUS_REASON = IChatBaseComponent.translatable("disconnect.ignoring_status_request");
    private final MinecraftServer server;
    private final NetworkManager connection;

    public HandshakeListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.server = minecraftserver;
        this.connection = networkmanager;
    }

    @Override
    public void handleIntention(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        switch (packethandshakinginsetprotocol.intention()) {
            case LOGIN:
                this.connection.setClientboundProtocolAfterHandshake(ClientIntent.LOGIN);
                if (packethandshakinginsetprotocol.protocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
                    IChatMutableComponent ichatmutablecomponent;

                    if (packethandshakinginsetprotocol.protocolVersion() < 754) {
                        ichatmutablecomponent = IChatBaseComponent.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().getName());
                    } else {
                        ichatmutablecomponent = IChatBaseComponent.translatable("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
                    }

                    this.connection.send(new PacketLoginOutDisconnect(ichatmutablecomponent));
                    this.connection.disconnect(ichatmutablecomponent);
                } else {
                    this.connection.setListener(new LoginListener(this.server, this.connection));
                }
                break;
            case STATUS:
                ServerPing serverping = this.server.getStatus();

                if (this.server.repliesToStatus() && serverping != null) {
                    this.connection.setClientboundProtocolAfterHandshake(ClientIntent.STATUS);
                    this.connection.setListener(new PacketStatusListener(serverping, this.connection));
                } else {
                    this.connection.disconnect(HandshakeListener.IGNORE_STATUS_REASON);
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.intention());
        }

    }

    @Override
    public void onDisconnect(IChatBaseComponent ichatbasecomponent) {}

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
