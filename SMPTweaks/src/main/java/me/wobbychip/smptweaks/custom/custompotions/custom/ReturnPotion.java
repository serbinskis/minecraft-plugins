package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class ReturnPotion extends CustomPotion {
	public ReturnPotion() {
		super("recall", Material.ENDER_EYE, "return", Color.fromRGB(129, 111, 179));
		this.setDisplayName("§r§fPotion of Return");
		this.setLore(List.of("§9Teleports to Deathpoint"));
		this.setTippedArrow(true, "§r§fArrow of Return");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		teleportPlayer(event.getPlayer());
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { teleportPlayer((Player) livingEntity); }
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { teleportPlayer((Player) livingEntity); }
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() instanceof Player) {
				teleportPlayer((Player) event.getHitEntity());
			}
		}
	}

	public void teleportPlayer(Player player) {
		Location location = player.getLastDeathLocation();
		if (location == null) { return; }

		location.setDirection(player.getLocation().getDirection());
		player.setVelocity(new Vector(0, 0, 0));
		player.setFallDistance(0);
		player.teleport(location);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
	}
}
