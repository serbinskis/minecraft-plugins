package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.custom.custompotions.potions.PotionManager;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;

import java.util.List;

public class FirePotion extends CustomPotion {
	public FirePotion() {
		super(PotionManager.getPotion(PotionType.AWKWARD, false, false), Material.BLAZE_ROD, "fire", Color.fromRGB(226, 88, 34));
		this.setDisplayName("§r§fPotion of Fire");
		this.setLore(List.of("§9Sets on fire"));
		this.setTippedArrow(true, "§r§fArrow of Fire");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		event.getPlayer().setFireTicks(20*10);
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			livingEntity.setFireTicks(20*5);
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			livingEntity.setFireTicks(20*1);
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() != null) {
				event.getHitEntity().setFireTicks(20*5);
			}
		}
	}
}
