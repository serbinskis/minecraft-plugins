package me.wobbychip.smptweaks.custom.chunkloader.events;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;

public class PotionEvents implements Listener {
	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		Field field = ReflectionUtils.getField(PotionSplashEvent.class, Map.class, true);
		Map<LivingEntity, Double> affectedEntities = (Map<LivingEntity, Double>) ReflectionUtils.getValue(field, event);
		Iterator<Entry<LivingEntity, Double>> iterator = affectedEntities.entrySet().iterator();

		while (iterator.hasNext()) {
			if (!isChunkLoader(iterator.next().getKey())) { continue; }
			iterator.remove();
		}
	}

	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		Field field = ReflectionUtils.getField(AreaEffectCloudApplyEvent.class, List.class, true);
		List<LivingEntity> affectedEntities = (List<LivingEntity>) ReflectionUtils.getValue(field, event);
		Iterator<LivingEntity> iterator = affectedEntities.iterator();

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