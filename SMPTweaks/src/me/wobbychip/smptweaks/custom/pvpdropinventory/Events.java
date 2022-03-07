package me.wobbychip.smptweaks.custom.pvpdropinventory;

import java.util.Arrays;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import me.wobbychip.smptweaks.Utils;

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
		addPlayers((Player) event.getEntity(), getAttacker(event.getDamager()));
	}

	public Player getAttacker(Entity entity) {
		if (entity instanceof Player) { return ((Player) entity); }

		if ((entity instanceof Projectile)) {
			ProjectileSource attacker = ((Projectile) entity).getShooter();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof AreaEffectCloud)) {
			ProjectileSource attacker = ((AreaEffectCloud) entity).getSource();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof TNTPrimed)) {
			Entity attacker = ((TNTPrimed) entity).getSource();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof Tameable)) {
			AnimalTamer attacker = ((Tameable) entity).getOwner();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		return null;
	}

	public void addPlayers(Player player1, Player player2) {
		if ((player1 == null) || (player2 == null)) { return; }
		if (player1.getUniqueId().equals(player2.getUniqueId())) { return; }

		PvPDropInventory.timer.addPlayer(player1, PvPDropInventory.timeout);
		PvPDropInventory.timer.sendActionMessage(player1);

		PvPDropInventory.timer.addPlayer(player2, PvPDropInventory.timeout);
		PvPDropInventory.timer.sendActionMessage(player2);
	}
}