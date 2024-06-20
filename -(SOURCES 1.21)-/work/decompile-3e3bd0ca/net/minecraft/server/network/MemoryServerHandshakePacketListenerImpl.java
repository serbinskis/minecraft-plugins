package net.minecraft.server.network;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.PacketHandshakingInListener;
import net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.server.MinecraftServer;

public class MemoryServerHandshakePacketListenerImpl implements PacketHandshakingInListener {

    private final MinecraftServer server;
    private final NetworkManager connection;

    public MemoryServerHandshakePacketListenerImpl(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.server = minecraftserver;
        this.connection = networkmanager;
    }

    @Override
    public void handleIntention(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        if (packethandshakinginsetprotocol.intention() != ClientIntent.LOGIN) {
            throw new UnsupportedOperationException("Invalid intention " + String.valueOf(packethandshakinginsetprotocol.intention()));
        } else {
            this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new LoginListener(this.server, this.connection, false));
            this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        }
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectiondetails) {}

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
