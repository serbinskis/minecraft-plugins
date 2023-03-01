package me.wobbychip.smptweaks.custom.noendportal;

import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPortalEvent(EntityPortalEvent event) {
		if (event.getTo().getWorld().getEnvironment() != Environment.THE_END) { return; }
		if (NoEndPortal.tweak.getGameRuleBoolean(event.getEntity().getWorld())) { return; }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		if (event.getTo().getWorld().getEnvironment() != Environment.THE_END) { return; }
		if (NoEndPortal.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		event.setCancelled(true);
	}
}
