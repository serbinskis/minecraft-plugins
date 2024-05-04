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

// CraftBukkit start
import java.net.InetAddress;
import java.util.HashMap;
// CraftBukkit end

public class HandshakeListener implements PacketHandshakingInListener {

    // CraftBukkit start - add fields
    private static final HashMap<InetAddress, Long> throttleTracker = new HashMap<InetAddress, Long>();
    private static int throttleCounter = 0;
    // CraftBukkit end
    private static final IChatBaseComponent IGNORE_STATUS_REASON = IChatBaseComponent.translatable("disconnect.ignoring_status_request");
    private final MinecraftServer server;
    private final NetworkManager connection;

    public HandshakeListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.server = minecraftserver;
        this.connection = networkmanager;
    }

    @Override
    public void handleIntention(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        this.connection.hostname = packethandshakinginsetprotocol.hostName() + ":" + packethandshakinginsetprotocol.port(); // CraftBukkit  - set hostname
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
        // CraftBukkit start - Connection throttle
        try {
            long currentTime = System.currentTimeMillis();
            long connectionThrottle = this.server.server.getConnectionThrottle();
            InetAddress address = ((java.net.InetSocketAddress) this.connection.getRemoteAddress()).getAddress();

            synchronized (throttleTracker) {
                if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - throttleTracker.get(address) < connectionThrottle) {
                    throttleTracker.put(address, currentTime);
                    IChatMutableComponent chatmessage = IChatBaseComponent.literal("Connection throttled! Please wait before reconnecting.");
                    this.connection.send(new PacketLoginOutDisconnect(chatmessage));
                    this.connection.disconnect(chatmessage);
                    return;
                }

                throttleTracker.put(address, currentTime);
                throttleCounter++;
                if (throttleCounter > 200) {
                    throttleCounter = 0;

                    // Cleanup stale entries
                    java.util.Iterator iter = throttleTracker.entrySet().iterator();
                    while (iter.hasNext()) {
                        java.util.Map.Entry<InetAddress, Long> entry = (java.util.Map.Entry) iter.next();
                        if (entry.getValue() > connectionThrottle) {
                            iter.remove();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger().debug("Failed to check connection throttle", t);
        }
        // CraftBukkit end
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
