package me.serbinskis.smptweaks.custom.custompotions.custom;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
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

import java.util.List;
import java.util.Random;

public class CopperPotion extends CustomPotion {
	public CopperPotion() {
		super(UnregisteredPotion.create(AmethystPotion.class), Material.COPPER_INGOT, "copper", Color.fromRGB(231, 124, 86));
		this.setDisplayName("§r§fPotion of Copper");
		this.setLore(List.of("§9Use when thunder"));
		this.setTippedArrow(true, "§r§fArrow of Copper");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		summonLighting(event.getPlayer().getLocation());
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		if (event.getAffectedEntities().isEmpty()) { return; }
		int i = new Random().nextInt(event.getAffectedEntities().size());
		summonLighting(((LivingEntity) event.getAffectedEntities().toArray()[i]).getLocation());
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (!event.getEntity().getWorld().isThundering()) { return; }

		if (event.getEntity() instanceof Arrow arrow) {
			Entity entity = (event.getHitEntity() != null) ? event.getHitEntity() : arrow;
			summonLighting(entity.getLocation());
			arrow.remove();
		}

		if (event.getEntity() instanceof ThrownPotion thrownPotion) {
			if (thrownPotion.getItem().getType() != Material.SPLASH_POTION) { return; }
			summonLighting(event.getEntity().getLocation());
		}
	}

	public void summonLighting(Location location) {
		if (!location.getWorld().isThundering()) { return; }
		location.getWorld().strikeLightning(location);
	}
}
