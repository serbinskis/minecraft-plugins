package me.wobbychip.pvpdropinventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (Main.timer.isPlayer(event.getEntity())) { 
			Main.timer.removePlayer(event.getEntity());
		} else {
			event.setKeepInventory(true);
			event.setKeepLevel(true);
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) { return; }
		if (!(event.getDamager() instanceof Player)) { return; }
		if (event.getEntity().getUniqueId().equals(event.getDamager().getUniqueId())) { return; }

		Main.timer.addPlayer((Player) event.getEntity(), Main.timeout);
		Main.timer.sendActionMessage((Player) event.getEntity());

		Main.timer.addPlayer((Player) event.getDamager(), Main.timeout);
		Main.timer.sendActionMessage((Player) event.getDamager());
	}
}