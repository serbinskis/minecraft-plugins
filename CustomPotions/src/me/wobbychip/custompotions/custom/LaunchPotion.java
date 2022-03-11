package me.wobbychip.custompotions.custom;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.potions.PotionManager;

public class LaunchPotion extends CustomPotion {
	public LaunchPotion() {
		super(PotionManager.getPotion(PotionType.AWKWARD, false, false), Material.SLIME_BLOCK, "launch", Color.fromRGB(0, 255, 0));
		this.setDisplayName("§r§fPotion of Launch");
		this.setLore(Arrays.asList("§9Yeet"));
		this.setTippedArrow(true, "§r§fArrow of Launch");
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		launchEntity(event.getPlayer(), 2);
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			launchEntity(livingEntity, 2);
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			launchEntity(livingEntity, 1);
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			launchEntity(event.getHitEntity(), 1);
		}
	}

	public void launchEntity(Entity entity, double power) {
		if (entity == null) { return; }
        double x = entity.getVelocity().getX();
        double y = entity.getVelocity().getY() + power;
        double z = entity.getVelocity().getZ();
        entity.setVelocity(new Vector(x, y, z));
	}
}
