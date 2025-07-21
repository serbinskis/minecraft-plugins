package me.serbinskis.smptweaks.custom.custompotions.events;

import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.PotionManager;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		CustomPotion customPotion = PotionManager.getCustomPotion(event.getItem());
		if ((customPotion == null) || !customPotion.isEnabled()) { return; }
		customPotion.onPotionConsume(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		CustomPotion customPotion = PotionManager.getCustomPotion(event.getEntity().getItem());
		if ((customPotion == null) || !customPotion.isEnabled()) { return; }
		customPotion.onPotionSplash(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
		CustomPotion customPotion = PotionManager.getCustomPotion(event.getEntity().getItem());
		if (customPotion == null) { return; }
		if (customPotion.isEnabled()) { customPotion.onLingeringPotionSplash(event); }

		if (!event.isCancelled()) {
			AreaEffectCloud effectCloud = event.getAreaEffectCloud();
			effectCloud.clearCustomEffects();
			if (customPotion.getCloudEffect() != null) { effectCloud.addCustomEffect(customPotion.getCloudEffect(), true); }
			effectCloud.addCustomEffect(new PotionEffect(PotionEffectType.UNLUCK, 0, 0), false); //This is needed to trigger AreaEffectCloudApplyEvent
			PersistentUtils.setPersistentDataString(effectCloud, CustomPotions.TAG_CUSTOM_POTION, customPotion.getName());
			effectCloud.setColor(customPotion.getColor());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		CustomPotion customPotion = PotionManager.getCustomPotion(event.getEntity());
		if ((customPotion == null) || !customPotion.isEnabled()) { return; }
		customPotion.onAreaEffectCloudApply(event);
	}

	//Dispenser doesn't work because bukkit is again garbage and doesn't even apply metadata from item to arrow
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof ThrownPotion thrownPotion) {
			CustomPotion customPotion = PotionManager.getCustomPotion(thrownPotion.getItem());
			if (customPotion != null) { PersistentUtils.setPersistentDataString(thrownPotion, CustomPotions.TAG_CUSTOM_POTION, customPotion.getName()); }
		}

		if (event.getEntity() instanceof Arrow arrow) {
			CustomPotion customPotion = PotionManager.getCustomPotion(event.getEntity());
			if ((customPotion != null) && (customPotion.getArrowEffect() != null)) { arrow.addCustomEffect(customPotion.getArrowEffect(), true); }
		}

		CustomPotion customPotion = PotionManager.getCustomPotion(event.getEntity());
		if ((customPotion == null) || !customPotion.isEnabled()) { return; }
		customPotion.onProjectileHit(event);
	}
}