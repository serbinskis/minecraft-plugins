package me.wobbychip.custompotions.custom;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.potions.PotionManager;

public class MilkPotion extends CustomPotion {
	public MilkPotion() {
		super(PotionManager.getPotion(PotionType.AWKWARD, false, false), Material.MILK_BUCKET, "milk", Color.fromRGB(252, 252, 252));
		this.setDisplayName("§r§fPotion of Milk");
		this.setLore(Arrays.asList("§9Removes all effects"));
		this.setTippedArrow(true, "§r§fArrow of Milk");
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		removeEffects(event.getPlayer());
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			removeEffects(livingEntity);
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			removeEffects(livingEntity);
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() instanceof LivingEntity) {
				removeEffects((LivingEntity) event.getHitEntity());
			}
		}
	}

	public void removeEffects(LivingEntity entity) {
		for (PotionEffect effect : entity.getActivePotionEffects()) {
			entity.removePotionEffect(effect.getType());
		}
	}
}
