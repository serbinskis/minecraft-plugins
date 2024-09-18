package me.serbinskis.smptweaks.custom.pvpdropinventory;

import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
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
		if ((!PvPDropInventory.tweak.getGameRuleBoolean(event.getEntity().getWorld()))) { return; }
		Player attacker = Utils.getAttacker(event.getDamager());
		if ((attacker != null) && (attacker.getGameMode() == GameMode.CREATIVE)) { return; }
		PvPDropInventory.timer.addPlayers((Player) event.getEntity(), attacker);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
		if (!(event.getEntity() instanceof Player)) { return; }
		boolean isPlayer = PvPDropInventory.timer.isPlayer((Player) event.getEntity());
		if (PvPDropInventory.elytraAllowed || !event.isGliding() || !isPlayer) { return; }
		PvPDropInventory.timer.addTried((Player) event.getEntity());
		Utils.sendActionMessage((Player) event.getEntity(), PvPDropInventory.elytraBarMessage);
		event.setCancelled(true);
	}
}