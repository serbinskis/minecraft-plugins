package me.wobbychip.smptweaks.custom.custompotions.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.custom.custompotions.potions.PotionManager;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class LightingPotion extends CustomPotion {
	public List<UUID> isAffected = new ArrayList<>();;

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

	public void onPotionSplash(PotionSplashEvent event) {
		Collection<LivingEntity> affectedEntities = event.getAffectedEntities().stream().filter(e -> {
			Utils.sendMessage((e instanceof Player) ? ((Player) e).isCollidable() : "null");
			return !isChunkLoader(e);
		}).collect(Collectors.toList());

		if (affectedEntities.size() <= 0) { return; }
		int i = new Random().nextInt(affectedEntities.size());
		summonLighting(((LivingEntity) affectedEntities.toArray()[i]).getLocation());
		isAffected.add(event.getPotion().getUniqueId());

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				isAffected.remove(event.getPotion().getUniqueId());
			}
		}, 1L);
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		Collection<LivingEntity> affectedEntities = event.getAffectedEntities().stream().filter(e -> {
			return !isChunkLoader(e);
		}).collect(Collectors.toList());

		if (affectedEntities.size() <= 0) { return; }
		int i = new Random().nextInt(affectedEntities.size());
		summonLighting(((LivingEntity) affectedEntities.toArray()[i]).getLocation());
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		//0L - Runnable will run in the same tick, but in less priority after all events
		//Which means onPotionSplash will run first and this one after
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				if (isAffected.contains(event.getEntity().getUniqueId())) { return; }

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
		}, 0L);
	}

	public void summonLighting(Location location) {
		location.getWorld().strikeLightning(location);
	}

	public boolean isChunkLoader(LivingEntity entity) {
		boolean flag1 = ((entity instanceof Player) && ((Player) entity).isCollidable());
		boolean flag2 = PersistentUtils.hasPersistentDataBoolean(entity, ChunkLoader.isChunkLoader);
		return flag1 || flag2;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLightningStrikeEvent(LightningStrikeEvent event) {
		Block block = event.getLightning().getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (block.getType() != Material.BREWING_STAND) { return; }
		PotionManager.convertPotion("copper", "lighting", (BrewingStand) block.getState());
	}
}
