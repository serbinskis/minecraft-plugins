package me.wobbychip.pvpdropinventory;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!event.getKeepInventory()) { return; }
		if (!event.getKeepLevel()) { return; }

		if (Main.timer.isPlayer(event.getEntity())) { 
			Main.timer.removePlayer(event.getEntity());

			event.setDroppedExp(Utils.getExperienceReward(event.getEntity(), Main.dropAllXp));
			ItemStack[] items = event.getEntity().getInventory().getContents();
			event.getDrops().addAll(Arrays.asList(items));

			event.getEntity().setLevel(0);
			event.getEntity().setExp(0);
			event.getEntity().getInventory().clear();
			event.setKeepInventory(false);
			event.setKeepLevel(false);
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