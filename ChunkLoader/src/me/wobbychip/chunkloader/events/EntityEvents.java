package me.wobbychip.chunkloader.events;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityEvents implements Listener {
	@EventHandler(priority=EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event) {
		if ((event.getEntity().getType() == EntityType.SHULKER) && event.getEntity().isInvulnerable()) {
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}
}
