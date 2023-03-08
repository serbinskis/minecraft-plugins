package me.wobbychip.smptweaks.custom.pvpdropinventory;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		boolean isPlayer = PvPDropInventory.timer.isPlayer(event.getEntity());

		if (isPlayer) {
			PvPDropInventory.timer.removePlayer(event.getEntity());
			event.setDroppedExp(Utils.getExperienceReward(event.getEntity(), PvPDropInventory.dropAllXp));
			event.getEntity().setLevel(0);
			event.getEntity().setExp(0);
			event.setKeepLevel(false);
		}

		if (isPlayer && event.getKeepInventory()) {
			ItemStack[] items = event.getEntity().getInventory().getContents();
			event.getDrops().addAll(Arrays.asList(items));
			event.getEntity().getInventory().clear();
			event.setKeepInventory(false);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) { return; }
		PvPDropInventory.timer.addPlayers((Player) event.getEntity(), Utils.getAttacker(event.getDamager()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityToggleGlide(EntityToggleGlideEvent event) {
		if (!(event.getEntity() instanceof Player)) { return; }
		boolean isPlayer = PvPDropInventory.timer.isPlayer((Player) event.getEntity());
		if (PvPDropInventory.elytraAllowed || !event.isGliding() || !isPlayer) { return; }
		PvPDropInventory.timer.addTried((Player) event.getEntity());
		Utils.sendActionMessage((Player) event.getEntity(), PvPDropInventory.elytraBarMessage);
		event.setCancelled(true);
	}
}