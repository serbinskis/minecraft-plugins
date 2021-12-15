package me.wobbychip.custompotions.events;

import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.wobbychip.custompotions.Main;
import me.wobbychip.custompotions.potions.CustomPotion;

public class PotionEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getItem());
		if (customPotion != null) { customPotion.onPotionConsume(event); }
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getEntity().getItem());
		if (customPotion != null) { customPotion.onPotionSplash(event); }
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {		
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getEntity().getItem());
		if (customPotion == null) { return; }
		customPotion.onLingeringPotionSplash(event);

		if (!event.isCancelled()) {
			AreaEffectCloud effectCloud = event.getAreaEffectCloud();
			effectCloud.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 0, 0), false);
			effectCloud.setCustomName(customPotion.getName());
			effectCloud.setCustomNameVisible(false);
			effectCloud.setColor(customPotion.getColor());
		}
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getEntity().getCustomName());
		if (customPotion != null) { customPotion.onAreaEffectCloudApply(event); }
    }

	//Dispenser doesn't work because bukkit is again garbage and doesn't even apply metdata from item to arrow
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof ThrownPotion) {
			CustomPotion customPotion = Main.manager.getCustomPotion(((ThrownPotion) event.getEntity()).getItem());
			if (customPotion != null) { event.getEntity().setCustomName(customPotion.getName()); }
		}

		CustomPotion customPotion = Main.manager.getCustomPotion(event.getEntity());
		if (customPotion != null) { customPotion.onProjectileHit(event); }
    }
}