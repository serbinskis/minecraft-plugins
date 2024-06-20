package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.DisconnectionDetails;
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

    // Spigot start
    private static final com.google.gson.Gson gson = new com.google.gson.Gson();
    static final java.util.regex.Pattern HOST_PATTERN = java.util.regex.Pattern.compile("[0-9a-f\\.:]{0,45}");
    static final java.util.regex.Pattern PROP_PATTERN = java.util.regex.Pattern.compile("\\w{0,16}");
    // Spigot end
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
                    this.connection.disconnect((IChatBaseComponent) ichatmutablecomponent);
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

            if (packethandshakinginsetprotocol.protocolVersion() < SharedConstants.getCurrentVersion().getProtocolVersion()) { // Spigot - SPIGOT-7546: Handle version check correctly for outdated client message
                ichatmutablecomponent = IChatBaseComponent.literal( java.text.MessageFormat.format( org.spigotmc.SpigotConfig.outdatedClientMessage.replaceAll("'", "''"), SharedConstants.getCurrentVersion().getName() ) ); // Spigot
            } else {
                ichatmutablecomponent = IChatBaseComponent.literal( java.text.MessageFormat.format( org.spigotmc.SpigotConfig.outdatedServerMessage.replaceAll("'", "''"), SharedConstants.getCurrentVersion().getName() ) ); // Spigot
            }

            this.connection.send(new PacketLoginOutDisconnect(ichatmutablecomponent));
            this.connection.disconnect((IChatBaseComponent) ichatmutablecomponent);
        } else {
            this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new LoginListener(this.server, this.connection, flag));
            // Spigot Start
            String[] split = packethandshakinginsetprotocol.hostName().split("\00");
            if (org.spigotmc.SpigotConfig.bungee) {
                if ( ( split.length == 3 || split.length == 4 ) && ( HOST_PATTERN.matcher( split[1] ).matches() ) ) {
                    connection.hostname = split[0];
                    connection.address = new java.net.InetSocketAddress(split[1], ((java.net.InetSocketAddress) connection.getRemoteAddress()).getPort());
                    connection.spoofedUUID = com.mojang.util.UndashedUuid.fromStringLenient( split[2] );
                } else
                {
                    IChatBaseComponent chatmessage = IChatBaseComponent.literal("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                    this.connection.send(new PacketLoginOutDisconnect(chatmessage));
                    this.connection.disconnect(chatmessage);
                    return;
                }
                if ( split.length == 4 )
                {
                    connection.spoofedProfile = gson.fromJson(split[3], com.mojang.authlib.properties.Property[].class);
                }
            } else if ( ( split.length == 3 || split.length == 4 ) && ( HOST_PATTERN.matcher( split[1] ).matches() ) ) {
                IChatBaseComponent chatmessage = IChatBaseComponent.literal("Unknown data in login hostname, did you forget to enable BungeeCord in spigot.yml?");
                this.connection.send(new PacketLoginOutDisconnect(chatmessage));
                this.connection.disconnect(chatmessage);
                return;
            }
            // Spigot End
        }

    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectiondetails) {}

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
