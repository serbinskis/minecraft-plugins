package me.wobbychip.smptweaks.custom.noendportal;

import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.wobbychip.smptweaks.Main;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPortalEvent(EntityPortalEvent event) {
		if (event.getTo().getWorld().getEnvironment() != Environment.THE_END) { return; }
		if ((boolean) Main.gameRules.getGameRule(event.getEntity().getWorld(), "doEndPortal")) { return; }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		if (event.getTo().getWorld().getEnvironment() != Environment.THE_END) { return; }
		if ((boolean) Main.gameRules.getGameRule(event.getPlayer().getWorld(), "doEndPortal")) { return; }
		event.setCancelled(true);
	}
}
