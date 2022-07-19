package me.wobbychip.custompotions.custom;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.util.Vector;

import me.wobbychip.custompotions.potions.CustomPotion;

public class RecallPotion extends CustomPotion {
	public RecallPotion() {
		super("amethyst", Material.CHORUS_FRUIT, "recall", Color.fromRGB(23, 193, 224));
		this.setDisplayName("§r§fPotion of Recalling");
		this.setLore(Arrays.asList("§9Teleport to Spawnpoint"));
		this.setTippedArrow(true, "§r§fArrow of Recalling");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		respawnPlayer(event.getPlayer());
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { respawnPlayer((Player) livingEntity); }
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { respawnPlayer((Player) livingEntity); }
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() instanceof Player) {
				respawnPlayer((Player) event.getHitEntity());
			}
		}
	}

	public void respawnPlayer(Player player) {
		Location location = player.getBedSpawnLocation();

		if (location == null) {
			World world = Bukkit.getServer().getWorlds().get(0);
			location = world.getSpawnLocation().clone().add(.5, 0, .5);
			while ((location.getY() >= world.getMinHeight()) && (location.getBlock().getType() == Material.AIR)) { location.setY(location.getY()-1); }
			while ((location.getY() < world.getMaxHeight()) && (location.getBlock().getType() != Material.AIR)) { location.setY(location.getY()+1); }
		}

		location.setDirection(player.getLocation().getDirection());
		player.setVelocity(new Vector(0, 0, 0));
		player.setFallDistance(0);
		player.teleport(location);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
	}
}
