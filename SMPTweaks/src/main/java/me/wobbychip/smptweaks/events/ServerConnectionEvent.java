package me.wobbychip.smptweaks.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ServerConnectionEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final int connections;
	private final boolean online;

	public ServerConnectionEvent(int connections, boolean online) {
		this.connections  = connections;
		this.online  = online;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public int getConnections() {
		return this.connections;
	}

	public boolean isOnline() {
		return this.online;
	}
}