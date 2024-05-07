package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.handshake.PacketHandshakingInListener;
import net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.login.PacketLoginOutDisconnect;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.network.protocol.status.StatusProtocols;
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
                this.beginLogin(packethandshakinginsetprotocol, false);
                break;
            case STATUS:
                ServerPing serverping = this.server.getStatus();

                this.connection.setupOutboundProtocol(StatusProtocols.CLIENTBOUND);
                if (this.server.repliesToStatus() && serverping != null) {
                    this.connection.setupInboundProtocol(StatusProtocols.SERVERBOUND, new PacketStatusListener(serverping, this.connection));
                } else {
                    this.connection.disconnect(HandshakeListener.IGNORE_STATUS_REASON);
                }
                break;
            case TRANSFER:
                if (!this.server.acceptsTransfers()) {
                    this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
                    IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable("multiplayer.disconnect.transfers_disabled");

                    this.connection.send(new PacketLoginOutDisconnect(ichatmutablecomponent));
                    this.connection.disconnect(ichatmutablecomponent);
                } else {
                    this.beginLogin(packethandshakinginsetprotocol, true);
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + String.valueOf(packethandshakinginsetprotocol.intention()));
        }

    }

    private void beginLogin(PacketHandshakingInSetProtocol packethandshakinginsetprotocol, boolean flag) {
        this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
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
            this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new LoginListener(this.server, this.connection, flag));
        }

    }

    @Override
    public void onDisconnect(IChatBaseComponent ichatbasecomponent) {}

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
