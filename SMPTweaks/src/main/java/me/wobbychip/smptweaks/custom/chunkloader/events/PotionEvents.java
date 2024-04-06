package me.wobbychip.smptweaks.custom.chunkloader.events;

import me.wobbychip.smptweaks.custom.chunkloader.loaders.FakePlayer;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionEvents implements Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		ReflectionUtils.getAffectedEntities(event).keySet().removeIf(e -> FakePlayer.isFakePlayer(e.getUniqueId()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        ReflectionUtils.getAffectedEntities(event).removeIf(e -> FakePlayer.isFakePlayer(e.getUniqueId()));
	}
}