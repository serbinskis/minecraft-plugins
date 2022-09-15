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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.PersistentUtils;

public class CopperPotion extends CustomPotion {
	public List<UUID> isAffected = new ArrayList<>();;

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

	public void onPotionSplash(PotionSplashEvent event) {
		Collection<LivingEntity> affectedEntities = event.getAffectedEntities().stream().filter(e -> {
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
		if (!event.getEntity().getWorld().isThundering()) { return; }

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
		if (!location.getWorld().isThundering()) { return; }
		location.getWorld().strikeLightning(location);
	}

	public boolean isChunkLoader(LivingEntity entity) {
		boolean flag1 = ((entity instanceof Player) && ((Player) entity).isCollidable());
		boolean flag2 = PersistentUtils.hasPersistentDataBoolean(entity, ChunkLoader.isChunkLoader);
		return flag1 || flag2;
	}
}
