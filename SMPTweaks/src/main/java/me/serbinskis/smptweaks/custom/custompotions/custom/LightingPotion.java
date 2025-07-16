package me.serbinskis.smptweaks.custom.custompotions.custom;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.PotionManager;
import me.serbinskis.smptweaks.custom.custompotions.potions.UnregisteredPotion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import java.util.List;
import java.util.Random;

public class LightingPotion extends CustomPotion {
	public LightingPotion() {
		super(UnregisteredPotion.create(CopperPotion.class), null, "lighting", Color.fromRGB(197, 235, 252));
		this.setDisplayName("§r§fPotion of Lighting");
		this.setLore(List.of("§9Strike brewing stand with lighting"));
		this.setTippedArrow(true, "§r§fArrow of Lighting");
		this.setAllowVillagerTrades(true);
	}

	@Override
	public boolean onAffectLivingEntity(LivingEntity livingEntity, Event event) {
		if (event instanceof AreaEffectCloudApplyEvent areaEffectCloudApplyEvent) {
			int i = new Random().nextInt(areaEffectCloudApplyEvent.getAffectedEntities().size());
			summonLighting(areaEffectCloudApplyEvent.getAffectedEntities().get(i).getLocation());
			return false;
		}

		summonLighting(livingEntity.getLocation());
		return true;
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if ((event.getEntity() instanceof Arrow arrow) && (event.getHitEntity() == null)) {
			summonLighting(arrow.getLocation());
			arrow.remove();
		}

		super.onProjectileHit(event);
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
		PotionManager.convertPotion(getBase().getName(), getName(), (BrewingStand) block.getState());
	}
}
