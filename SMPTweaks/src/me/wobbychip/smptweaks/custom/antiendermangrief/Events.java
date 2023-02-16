package me.wobbychip.smptweaks.custom.antiendermangrief;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import me.wobbychip.smptweaks.Main;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if ((boolean) Main.gameRules.getGameRule(event.getEntity().getWorld(), "doEndermanGrief")) { return; }
		if (event.getEntityType() == EntityType.ENDERMAN) { event.setCancelled(true); }
	}
}
