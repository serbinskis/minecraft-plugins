package me.wobbychip.smptweaks.custom.custompotions.custom;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;

public class CopperPotion extends CustomPotion {
	public CopperPotion() {
		super("amethyst", Material.COPPER_INGOT, "copper", Color.fromRGB(231, 124, 86));
		this.setDisplayName("§r§fPotion of Copper");
		this.setLore(Arrays.asList("§9Use when thunder"));
		this.setTippedArrow(true, "§r§fArrow of Copper");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		summonLighting(event.getPlayer().getLocation());
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		if (event.getAffectedEntities().size() <= 0) { return; }
		int i = new Random().nextInt(event.getAffectedEntities().size());
		summonLighting(((LivingEntity) event.getAffectedEntities().toArray()[i]).getLocation());
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (!event.getEntity().getWorld().isThundering()) { return; }

		if (event.getEntity() instanceof Arrow) {
			Entity entity = (event.getHitEntity() != null) ? event.getHitEntity() : event.getEntity();
			summonLighting(entity.getLocation());
			event.getEntity().remove();
		}

		if (event.getEntity() instanceof ThrownPotion) {
			if (((ThrownPotion) event.getEntity()).getItem().getType() != Material.SPLASH_POTION) { return; }
			summonLighting(event.getEntity().getLocation());
		}
	}

	public void summonLighting(Location location) {
		if (!location.getWorld().isThundering()) { return; }
		location.getWorld().strikeLightning(location);
	}
}
