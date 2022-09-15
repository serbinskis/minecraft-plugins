package me.wobbychip.smptweaks.custom.custompotions.custom;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.custom.custompotions.potions.PotionManager;

public class LightingPotion extends CustomPotion {
	public LightingPotion() {
		super("copper", null, "lighting", Color.fromRGB(197, 235, 252));
		this.setDisplayName("§r§fPotion of Lighting");
		this.setLore(Arrays.asList("§9Strike brewing stand with lighting"));
		this.setTippedArrow(true, "§r§fArrow of Lighting");
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
		location.getWorld().strikeLightning(location);
	}

	public Block getAttached(Block block) {
		LightningRod rod = (LightningRod) block.getBlockData();
		return block.getRelative(rod.getFacing().getOppositeFace());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLightningStrikeEvent(LightningStrikeEvent event) {
		Block brewing = event.getLightning().getLocation().getBlock();
		Block rod = event.getLightning().getLocation().getBlock().getRelative(BlockFace.DOWN);
		Block block = (rod.getType() == Material.LIGHTNING_ROD) ? getAttached(rod) : brewing;
		if (block.getType() != Material.BREWING_STAND) { return; }
		PotionManager.convertPotion("copper", "lighting", (BrewingStand) block.getState());
	}
}
