package me.wobbychip.custompotions.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.utils.Utils;

public class LeadPotion extends CustomPotion {
	public static int MAX_LEADS = 10;

	public LeadPotion() {
		super("base", Material.LEAD, "lead", Color.fromRGB(164, 102, 60));
		this.setDisplayName("§r§fPotion of Lead");
		this.setLore(Arrays.asList("§9Leads nearby mobs"));
		this.setTippedArrow(true, "§r§fArrow of Lead");
	}

	public void onPotionSplash(PotionSplashEvent event) {
		ProjectileSource source = event.getEntity().getShooter();
		if (!(source instanceof Player)) { return; }

		Location loc = ((Player) source).getLocation();
		ArrayList<LivingEntity> list = new ArrayList<>(event.getAffectedEntities());
		Collections.sort(list, (a, b) -> (int) (Utils.distance(loc, a.getLocation()) - Utils.distance(loc, b.getLocation())));
		int counter = 0;

		for (LivingEntity entity : list) {
			if (leadEntity((Player) source, entity)) { counter++; }
			if (counter >= MAX_LEADS) { break; }
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		ProjectileSource source = ((AreaEffectCloud) event.getEntity()).getSource();
		if (!(source instanceof Player)) { return; }
		int counter = 0;

		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (leadEntity(((Player) source), livingEntity)) { counter++; }
			if (counter >= MAX_LEADS) { break; }
		}

		int duration = (event.getEntity().getDuration()/MAX_LEADS)*(MAX_LEADS-counter);
		event.getEntity().setDuration(duration);
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() instanceof LivingEntity) {
				ProjectileSource source = event.getEntity().getShooter();
				if ((source instanceof Player)) {
					leadEntity((Player) source, (LivingEntity) event.getHitEntity());
				}
			}
		}
	}

	public boolean leadEntity(Player player, LivingEntity entity) {
		if ((player == null) || (entity == null) || (entity instanceof Player) || entity.isLeashed()) { return false; }
		if (Utils.distance(player.getLocation(), entity.getLocation()) >= 10) { return false; }
		return entity.setLeashHolder(player);
	}
}
