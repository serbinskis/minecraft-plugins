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
		if (!event.getKeepInventory()) { return; }
		if (!Main.timer.isPlayer(event.getEntity())) { return; }
		Main.timer.removePlayer(event.getEntity());

		event.setKeepInventory(false);
		event.setKeepLevel(false);
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