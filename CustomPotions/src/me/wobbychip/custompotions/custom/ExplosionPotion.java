package me.wobbychip.custompotions.custom;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.potions.PotionManager;

public class ExplosionPotion extends CustomPotion {
	public ExplosionPotion() {
		super(PotionManager.getPotion(PotionType.AWKWARD, false, false), Material.TNT, "explosion", Color.fromRGB(255, 0, 0));
		this.setDisplayName("§r§fPotion of Explosion");
		this.setLore(Arrays.asList("§9Explodes"));
		this.setTippedArrow(true, "§r§fArrow of Explosion");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		World world = event.getPlayer().getLocation().getWorld();
		world.createExplosion(event.getPlayer().getLocation(), 2.0f);
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			World world = livingEntity.getLocation().getWorld();
			world.createExplosion(livingEntity.getLocation(), .75f, false, false);
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			World world = event.getEntity().getLocation().getWorld();
			world.createExplosion(event.getEntity().getLocation(), .75f, false, false);
			event.getEntity().remove();
		}

		if (event.getEntity() instanceof ThrownPotion) {
			if (((ThrownPotion) event.getEntity()).getItem().getType() != Material.SPLASH_POTION) { return; }
			World world = event.getEntity().getLocation().getWorld();
			world.createExplosion(event.getEntity().getLocation(), 2.0f);
		}
	}
}
