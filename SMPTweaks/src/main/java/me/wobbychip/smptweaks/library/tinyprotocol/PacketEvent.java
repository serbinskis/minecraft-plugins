package me.wobbychip.smptweaks.library.tinyprotocol;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PacketEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final PacketType.Flow flow;
    private final PacketType type;
    private Object packet;
    private boolean cancelled = false;

    public PacketEvent(Player player, PacketType.Flow flow, PacketType type, Object packet) {
        super(true);
        this.player = player;
        this.flow = flow;
        this.type = type;
        this.packet = packet;
    }

    public Player getPlayer() {
        return player;
    }

    public PacketType.Flow getFlow() {
        return flow;
    }

    public PacketType getType() {
        return type;
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
