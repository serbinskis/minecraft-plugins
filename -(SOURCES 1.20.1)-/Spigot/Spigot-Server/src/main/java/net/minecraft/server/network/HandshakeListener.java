package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.handshake.PacketHandshakingInListener;
import net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol;
import net.minecraft.network.protocol.login.PacketLoginOutDisconnect;
import net.minecraft.network.protocol.status.ServerPing;
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
        this.connection.hostname = packethandshakinginsetprotocol.hostName + ":" + packethandshakinginsetprotocol.port; // CraftBukkit  - set hostname
        switch (packethandshakinginsetprotocol.getIntention()) {
            case LOGIN:
                this.connection.setProtocol(EnumProtocol.LOGIN);
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
                if (packethandshakinginsetprotocol.getProtocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
                    IChatMutableComponent ichatmutablecomponent;

                    if (packethandshakinginsetprotocol.getProtocolVersion() < 754) {
                        ichatmutablecomponent = IChatBaseComponent.literal( java.text.MessageFormat.format( org.spigotmc.SpigotConfig.outdatedClientMessage.replaceAll("'", "''"), SharedConstants.getCurrentVersion().getName() ) ); // Spigot
                    } else {
                        ichatmutablecomponent = IChatBaseComponent.literal( java.text.MessageFormat.format( org.spigotmc.SpigotConfig.outdatedServerMessage.replaceAll("'", "''"), SharedConstants.getCurrentVersion().getName() ) ); // Spigot
                    }

                    this.connection.send(new PacketLoginOutDisconnect(ichatmutablecomponent));
                    this.connection.disconnect(ichatmutablecomponent);
                } else {
                    this.connection.setListener(new LoginListener(this.server, this.connection));
                    // Spigot Start
                    String[] split = packethandshakinginsetprotocol.hostName.split("\00");
                    if (org.spigotmc.SpigotConfig.bungee) {
                        if ( ( split.length == 3 || split.length == 4 ) && ( HOST_PATTERN.matcher( split[1] ).matches() ) ) {
                            packethandshakinginsetprotocol.hostName = split[0];
                            connection.address = new java.net.InetSocketAddress(split[1], ((java.net.InetSocketAddress) connection.getRemoteAddress()).getPort());
                            connection.spoofedUUID = com.mojang.util.UUIDTypeAdapter.fromString( split[2] );
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
                break;
            case STATUS:
                ServerPing serverping = this.server.getStatus();

                if (this.server.repliesToStatus() && serverping != null) {
                    this.connection.setProtocol(EnumProtocol.STATUS);
                    this.connection.setListener(new PacketStatusListener(serverping, this.connection));
                } else {
                    this.connection.disconnect(HandshakeListener.IGNORE_STATUS_REASON);
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.getIntention());
        }

    }

    @Override
    public void onDisconnect(IChatBaseComponent ichatbasecomponent) {}

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
