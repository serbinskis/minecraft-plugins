package me.wobbychip.custompotions.custom;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.potions.PotionManager;

public class LightingPotion extends CustomPotion {
	public LightingPotion() {
		super(PotionManager.getPotion(PotionType.AWKWARD, false, false), Material.SKELETON_SKULL, "lighting", Color.fromRGB(255, 255, 255));
		this.setDisplayName("§r§fPotion of Lighting");
		this.setLore(Arrays.asList("§9Summons lighting"));
		this.setTippedArrow(true, "§r§fArrow of Lighting");
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		summonLighting(event.getPlayer().getLocation(), 5, 1);
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			summonLighting(livingEntity.getLocation(), 1, 1);
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			summonLighting(event.getEntity().getLocation(), 1, 1);
			event.getEntity().remove();
		}

		if (event.getEntity() instanceof ThrownPotion) {
			if (((ThrownPotion) event.getEntity()).getItem().getType() != Material.SPLASH_POTION) { return; }
			summonLighting(event.getEntity().getLocation(), 5, 1);
		}
	}

	public void summonLighting(Location location, int amount, int delay) {
		for (int i = 0; i < amount; i++) {
			location.getWorld().strikeLightning(location);
		}
	}
}
