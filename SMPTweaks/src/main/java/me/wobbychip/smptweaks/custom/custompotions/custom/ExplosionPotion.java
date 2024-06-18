package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;

import java.util.List;

public class ExplosionPotion extends CustomPotion {
	public ExplosionPotion() {
		super(PotionType.AWKWARD, Material.TNT, "explosion", Color.fromRGB(255, 0, 0));
		this.setDisplayName("§r§fPotion of Explosion");
		this.setLore(List.of("§9Creates an explosion"));
		this.setTippedArrow(true, "§r§fArrow of Explosion");
		this.setAllowVillagerTrades(true);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		createExplosion(event.getPlayer(), event.getPlayer().getLocation(), 2.0f, false, true);
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			createExplosion(null, livingEntity.getLocation(), 0.75f, false, false);
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow arrow) {
			createExplosion((Player) arrow.getShooter(), arrow.getLocation(), 0.75f, false, false);
			arrow.remove();
		}

		if (event.getEntity() instanceof ThrownPotion thrownPotion) {
			if (thrownPotion.getItem().getType() != Material.SPLASH_POTION) { return; }
			createExplosion((Player) thrownPotion.getShooter(), thrownPotion.getLocation(), 2.0f, false, true);
		}
	}

	public void createExplosion(Player player, Location location, float power, boolean setFire, boolean breakBlocks) {
		if (!breakBlocks || (player == null)) {
			location.getWorld().createExplosion(location, power, setFire, false);
			return;
		}

		location.getWorld().spawn(location, TNTPrimed.class, tntPrimed -> {
			tntPrimed.setFuseTicks(-1);
			tntPrimed.setSource(player); //Needed for anti-grief plugins
			tntPrimed.setSilent(true);
			tntPrimed.setYield(power);
			tntPrimed.setIsIncendiary(setFire);
        });
	}
}
