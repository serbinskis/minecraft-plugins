package me.serbinskis.smptweaks.custom.custompotions.custom;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.UUID;

public class WormholePotion extends CustomPotion {
	public WormholePotion() {
		super(UnregisteredPotion.create(RecallPotion.class), Material.ECHO_SHARD, "wormhole", Color.fromRGB(120, 105, 235));
		this.setDisplayName("§r§fPotion of Wormhole");
		this.setLore(List.of("§9Shoot a player with arrow and then use the potion."));
		this.setTippedArrow(true, "§r§fArrow of Wormhole");
		this.setAllowVillagerTrades(true);
	}

	@Override
	public boolean onAffectPlayer(Player player, Event event) {
		if (event.getClass().equals(ProjectileHitEvent.class)) {
			saveTarget(player, ((ProjectileHitEvent) event).getEntity());
		} else {
			teleportAttacker(player);
		}

		return true;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		unmarkPlayer(event.getEntity());
	}

	public void saveTarget(Player target, Projectile arrow) {
		if (!(arrow.getShooter() instanceof Player attacker)) { return; }

		if (target.getUniqueId().equals(attacker.getUniqueId())) {
			Utils.sendActionMessage(target, "You cannot mark yourself.");
			return;
		}

		String uuid = target.getUniqueId().toString();
		String objectiveName = this.getName() + "_" + attacker.getUniqueId();
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		if (scoreboard.getObjective(objectiveName) == null) {
			scoreboard.registerNewObjective(objectiveName, Criteria.DUMMY, uuid);
		} else {
			scoreboard.getObjective(objectiveName).setDisplayName(uuid);
		}

		Utils.sendActionMessage(target, "You have been marked by " + attacker.getName() + ".");
		Utils.sendActionMessage(attacker, "You marked " + target.getName() + ".");
	}

	public void unmarkPlayer(Player player) {
		for (Objective objective : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
			if (!objective.getName().startsWith(this.getName() + "_")) { continue; }
			if (!objective.getDisplayName().equals(player.getUniqueId().toString())) { continue; }
			objective.unregister();
		}
	}

	public void teleportAttacker(Player player) {
		String objectiveName = this.getName() + "_" + player.getUniqueId();
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objectiveName);

		if (objective == null) {
			Utils.sendActionMessage(player, "You first need to mark a player.");
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(objective.getDisplayName()));

		if (!target.isOnline()) {
			Utils.sendActionMessage(player, target.getName() + " is not online.");
		} else {
			objective.unregister();
			Utils.sendActionMessage(player, "Wormhole'd to " + target.getName() + ".");
			Utils.sendActionMessage(target.getPlayer(), player.getName() + " wormhole'd to you.");
			player.teleport(target.getPlayer());
		}
	}
}
