package net.minecraft.server.network;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.PacketHandshakingInListener;
import net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol;
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
            throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.intention());
        } else {
            this.connection.setClientboundProtocolAfterHandshake(ClientIntent.LOGIN);
            this.connection.setListener(new LoginListener(this.server, this.connection));
        }
    }

    @Override
    public void onDisconnect(IChatBaseComponent ichatbasecomponent) {}

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
