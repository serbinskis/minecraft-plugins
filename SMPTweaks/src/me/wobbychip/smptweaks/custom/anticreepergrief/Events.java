package me.wobbychip.smptweaks.custom.anticreepergrief;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

import me.wobbychip.smptweaks.Main;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		if ((boolean) Main.gameRules.getGameRule(event.getEntity().getWorld(), "doCrepperGrief")) { return; }
		if (event.getEntityType() == EntityType.CREEPER) { event.blockList().clear(); }
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
		if (event.getCause() != RemoveCause.EXPLOSION) { return; }
		if (event.getRemover().getType() != EntityType.CREEPER) { return; }
		if ((boolean) Main.gameRules.getGameRule(event.getEntity().getWorld(), "doCrepperGrief")) { return; }
		event.setCancelled(true);
	}
}
