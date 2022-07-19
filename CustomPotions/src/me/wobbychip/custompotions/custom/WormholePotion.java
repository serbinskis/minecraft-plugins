package me.wobbychip.custompotions.custom;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.utils.Utils;

public class WormholePotion extends CustomPotion {
	public WormholePotion() {
		super("recall", Material.ENDER_EYE, "wormhole", Color.fromRGB(120, 105, 235));
		this.setDisplayName("§r§fPotion of Wormhole");
		this.setLore(Arrays.asList("§9Shoot a player and then use the potion."));
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

	public void saveTarget(Player target, Arrow arrow) {
		if (!(arrow.getShooter() instanceof Player)) { return; }
		Player attacker = (Player) arrow.getShooter();

		if (target.getUniqueId().equals(attacker.getUniqueId())) {
			Utils.sendActionMessage(target, "You cannot mark yourself.");
			return;
		}

		String uuid = target.getUniqueId().toString();
		String objectiveName = this.getName() + "_" + attacker.getUniqueId().toString();
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		if (scoreboard.getObjective(objectiveName) == null) {
			scoreboard.registerNewObjective(objectiveName, "dummy", uuid);
		} else {
			scoreboard.getObjective(objectiveName).setDisplayName(uuid);
		}

		Utils.sendActionMessage(target, "You have been marked by " + attacker.getName() + ".");
		Utils.sendActionMessage(attacker, "You marked " + target.getName() + ".");
	}

	public void teleportAttacker(Player player) {
		String objectiveName = this.getName() + "_" + player.getUniqueId().toString();
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
