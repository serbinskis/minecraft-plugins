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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.potions.PotionManager;

public class LeanPotion extends CustomPotion {
	public LeanPotion() {
		super(PotionManager.getPotion(PotionType.REGEN, false, false), Material.AMETHYST_SHARD, "lean", Color.fromRGB(139, 105, 202));
		this.setDisplayName("§r§fPotion of Lean");
		this.setLore(Arrays.asList("§9Lean is tasty"));
		this.setTippedArrow(true, "§r§fArrow of Lean");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		applyEffects(event.getPlayer());
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			applyEffects(livingEntity);
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			applyEffects(livingEntity);
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() instanceof LivingEntity) {
				applyEffects((LivingEntity) event.getHitEntity());
			}
		}
	}

	public void applyEffects(LivingEntity entity) {
		entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20*7, 0));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*5, 0));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*2, 0));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*5, 0));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20*5, 0));
	}
}
