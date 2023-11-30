package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.UUID;

public class WormholePotion extends CustomPotion {
	public WormholePotion() {
		super("recall", Material.ENDER_EYE, "wormhole", Color.fromRGB(120, 105, 235));
		this.setDisplayName("§r§fPotion of Wormhole");
		this.setLore(List.of("§9Shoot a player and then use the potion."));
		this.setTippedArrow(true, "§r§fArrow of Wormhole");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		teleportAttacker(event.getPlayer());
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { teleportAttacker((Player) livingEntity); }
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) {
				teleportAttacker((Player) livingEntity);
			}
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() instanceof Player) {
				saveTarget((Player) event.getHitEntity(), (Arrow) event.getEntity());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		unmarkPlayer(event.getPlayer());
	}

	public void saveTarget(Player target, Arrow arrow) {
		if (!(arrow.getShooter() instanceof Player attacker)) { return; }

		if (target.getUniqueId().equals(attacker.getUniqueId())) {
			Utils.sendActionMessage(target, "You cannot mark yourself.");
			return;
		}

		String uuid = target.getUniqueId().toString();
		String objectiveName = this.getName() + "_" + attacker.getUniqueId().toString();
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
