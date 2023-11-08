package me.wobbychip.smptweaks.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerConnectionEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private int connections;
	private boolean online;

	public ServerConnectionEvent(int connections, boolean online) {
		this.connections  = connections;
		this.online  = online;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public int getConnections() {
		return this.connections;
	}

	public boolean isOnline() {
		return this.online;
	}
}