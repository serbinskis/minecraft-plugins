package me.serbinskis.smptweaks.library.tinyprotocol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PacketEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final UUID playerId;
    private final PacketType.Flow packetFlow;
    private final PacketType packetType;
    private Object packet;
    private boolean cancelled = false;

    public PacketEvent(@Nullable UUID playerId, PacketType.Flow packetFlow, PacketType packetType, Object packet) {
        super(true);
        this.playerId = playerId;
        this.packetFlow = packetFlow;
        this.packetType = packetType;
        this.packet = packet;
    }

    public @Nullable Player getPlayer() {
        if (playerId == null) { return null; }
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) { player = PacketHandler.player_cache.get(playerId); }
        return player;
    }

    public PacketType.Flow getPacketFlow() {
        return packetFlow;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public Object getPacket() {
        return packet;
    }

    public void setPacket(Object packet) {
        this.packet = packet;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
