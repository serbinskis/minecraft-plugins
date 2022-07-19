package me.wobbychip.custompotions.custom;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.projectiles.ProjectileSource;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.utils.Utils;

public class TrackingPotion extends CustomPotion {
	public TrackingPotion() {
		super("base", Material.ENDER_EYE, "tracking", Color.fromRGB(50, 100, 100));
		this.setDisplayName("§r§fPotion of Tracking");
		this.setLore(Arrays.asList("§9Tracks nearest player"));
		this.setTippedArrow(false, "§r§fArrow of Tracking");
		this.setAllowVillagerTrades(false);
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		trackNearest(event.getPlayer());
	}

	public void onPotionSplash(PotionSplashEvent event) {
		ProjectileSource source = event.getEntity().getShooter();
		if ((source instanceof Player)) { trackNearest((Player) source); }
	}

	public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
		ProjectileSource source = event.getEntity().getShooter();
		if ((source instanceof Player)) { trackNearest((Player) source); }
		event.setCancelled(true);
	}

	public static Player getNearetPlayer(Player from) {
    	Player best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Player player : from.getWorld().getPlayers()) {
        	double distance = player.getLocation().distance(player.getLocation());

        	if ((distance < bestDistance) && !player.getUniqueId().equals(from.getUniqueId())) {
            	best = player;
                bestDistance = distance;
            }
        }

        return best;
	}

	public void trackNearest(Player player) {
		Player nearest = getNearetPlayer(player);

		if (nearest == null) {
			Utils.sendActionMessage(player, "No players found!");
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.5f, 1.5f);
		} else {
			EnderSignal signal = (EnderSignal) player.getWorld().spawnEntity(player.getLocation(), EntityType.ENDER_SIGNAL);
			signal.setTargetLocation(nearest.getLocation());
			signal.setDropItem(false);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1.5f, 1.5f);
			Utils.sendActionMessage(player, "Tracking: " + nearest.getName());
			Utils.sendActionMessage(nearest, "You are being tracked by " + player.getName() + "!");
		}
	}
}
