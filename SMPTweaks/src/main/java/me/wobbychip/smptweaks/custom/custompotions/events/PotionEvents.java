package me.wobbychip.smptweaks.custom.custompotions.events;

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

import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.PersistentUtils;

public class PotionEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getItem());
		if ((customPotion == null) || !customPotion.isEnabled()) { return; }
		customPotion.onPotionConsume(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getEntity().getItem());
		if ((customPotion == null) || !customPotion.isEnabled()) { return; }
		customPotion.onPotionSplash(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getEntity().getItem());
		if (customPotion == null) { return; }
		if (customPotion.isEnabled()) { customPotion.onLingeringPotionSplash(event); }

		if (!event.isCancelled()) {
			AreaEffectCloud effectCloud = event.getAreaEffectCloud();
			effectCloud.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 0, 0), false);
			PersistentUtils.setPersistentDataString(effectCloud, CustomPotions.customTag, customPotion.getName());
			effectCloud.setColor(customPotion.getColor());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getEntity());
		if ((customPotion == null) || !customPotion.isEnabled()) { return; }
		customPotion.onAreaEffectCloudApply(event);
	}

	//Dispenser doesn't work because bukkit is again garbage and doesn't even apply metadata from item to arrow
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof ThrownPotion) {
			CustomPotion customPotion = CustomPotions.manager.getCustomPotion(((ThrownPotion) event.getEntity()).getItem());
			if (customPotion != null) { PersistentUtils.setPersistentDataString(event.getEntity(), CustomPotions.customTag, customPotion.getName()); }			
		}

		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getEntity());
		if ((customPotion == null) || !customPotion.isEnabled()) { return; }
		customPotion.onProjectileHit(event);
	}
}