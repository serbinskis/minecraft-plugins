package me.wobbychip.smptweaks.custom.chunkloader.events;

import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionEvents implements Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
        ReflectionUtils.getAffectedEntities(event).entrySet().removeIf(e -> !isChunkLoader(e.getKey()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        ReflectionUtils.getAffectedEntities(event).removeIf(this::isChunkLoader);
	}

	public boolean isChunkLoader(LivingEntity entity) {
		boolean flag1 = ChunkLoader.manager.isFakePlayer(entity.getUniqueId());
		boolean flag2 = PersistentUtils.hasPersistentDataBoolean(entity, ChunkLoader.isChunkLoader);
		return flag1 || flag2;
	}
}