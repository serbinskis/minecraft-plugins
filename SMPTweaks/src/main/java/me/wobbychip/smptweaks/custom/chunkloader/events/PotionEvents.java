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

import java.util.Iterator;
import java.util.Map.Entry;

public class PotionEvents implements Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		Iterator<Entry<LivingEntity, Double>> iterator = ReflectionUtils.getAffectedEntities(event).entrySet().iterator();

		while (iterator.hasNext()) {
			if (!isChunkLoader(iterator.next().getKey())) { continue; }
			iterator.remove();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		Iterator<LivingEntity> iterator = ReflectionUtils.getAffectedEntities(event).iterator();

		while (iterator.hasNext()) {
			if (!isChunkLoader(iterator.next())) { continue; }
			iterator.remove();
		}
	}

	public boolean isChunkLoader(LivingEntity entity) {
		boolean flag1 = ChunkLoader.manager.isFakePlayer(entity.getUniqueId());
		boolean flag2 = PersistentUtils.hasPersistentDataBoolean(entity, ChunkLoader.isChunkLoader);
		return flag1 || flag2;
	}
}