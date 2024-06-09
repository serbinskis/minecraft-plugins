package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.custom.customworld.biomes.BiomeManager;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomBiome;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomWorld;
import me.wobbychip.smptweaks.library.tinyprotocol.PacketEvent;
import me.wobbychip.smptweaks.library.tinyprotocol.PacketType;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.World;
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
		Utils.fillChunk(event.getChunk(), Material.AIR, true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldInitEvent(WorldInitEvent event) {
		CustomBiome biome = BiomeManager.loadBiome(event.getWorld());
		if (biome != null) { BiomeManager.registerBiomeAll(biome); }

		if (!PersistentUtils.hasPersistentDataString(event.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD)) { return; }
		CustomWorld type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD));
		if ((type == null) || (type == CustomWorld.NONE)) { return; }
		if (event.getWorld().getEnvironment() == type.getEnvironment()) { return; }

		ReflectionUtils.setCustomDimension(event.getWorld(), null, type.getEnvironment(), null, null, null, null, null, null, null, null, type.getMinY(), null, null, null, null, null, null);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketEvent(PacketEvent event) {
		if (event.getPacketType() == PacketType.LEVEL_CHUNK_WITH_LIGHT) {
			World world = event.getPlayer().getWorld();
			CustomBiome cbiome = BiomeManager.getCustomBiome(world.getName());
			if ((cbiome == null) || (cbiome.isEmpty())) { return; }
			event.setPacket(ReflectionUtils.setPacketChunkBiome(world, event.getPacket(), cbiome.getNmsBiome(), cbiome.getName(), BiomeManager.getNMSMap()));
			return;
		}

		if ((event.getPacketType() != PacketType.LOGIN) && (event.getPacketType() != PacketType.RESPAWN)) { return; }
		World world = ReflectionUtils.getSpawnPacketWorld(event.getPacket());
		if (!PersistentUtils.hasPersistentDataString(world, CustomWorlds.TAG_CUSTOM_WORLD)) { return; }
		CustomWorld type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(world, CustomWorlds.TAG_CUSTOM_WORLD));
		if ((type == null) || (type == CustomWorld.NONE)) { return; }

		Object packet = ReflectionUtils.editSpawnPacket(event.getPacket(), type.isFlat(), type.getEnvironment());
		event.setPacket(packet); //PS: This will not work with overworld because end minY is 0 while overworld minY is -64
	}
}
