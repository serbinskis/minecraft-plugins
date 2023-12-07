package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.custom.customworld.biomes.BiomeManager;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomBiome;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomWorld;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkLoadEvent(ChunkLoadEvent event) {
		if (!event.isNewChunk()) { return; }
		if (!PersistentUtils.hasPersistentDataString(event.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD)) { return; }

		CustomWorld type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD));
		if ((type == null) || !type.isVoid()) { return; }
		ReflectionUtils.fillChunk(event.getChunk(), Material.AIR, true, false);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldInitEvent(WorldInitEvent event) {
		CustomBiome biome = BiomeManager.loadBiome(event.getWorld());
		if (biome != null) { BiomeManager.registerBiomeAll(biome); }

		if (!PersistentUtils.hasPersistentDataString(event.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD)) { return; }
		CustomWorld type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD));
		if ((type == null) || (type == CustomWorld.NONE)) { return; }

		ReflectionUtils.setCustomDimension(event.getWorld(), null, type.getEnvironment(), null, null, null, null, null, null, null, null, 0, null, null, null, null, null, null);
	}
}
