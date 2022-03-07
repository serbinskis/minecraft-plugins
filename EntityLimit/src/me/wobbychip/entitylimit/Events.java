package me.wobbychip.entitylimit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCreatureSpawnn(CreatureSpawnEvent event) {
		//Return if disabled or spawn reason is excluded
		if (!Main.pluginEnabled || Main.excludeReason.contains(event.getSpawnReason().toString())) {
			return;
		}

		//Check for entity limit
		if (Utilities.checkEntityLimit(event.getEntity().getType(), event.getEntity().getLocation())) {
			event.setCancelled(true);
		}
	}
}