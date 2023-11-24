package me.wobbychip.smptweaks.custom.chunkloader.loaders;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class Chunks {
	public static Map<String, Integer> chunks = new HashMap<>();

	public static void markChunks(Location location, int radius, boolean addOrRemove) {
		int pX = location.getChunk().getX();
		int pZ = location.getChunk().getZ();
		World world = location.getWorld();

		if (addOrRemove) {
			addChunk(location.getChunk());
		} else {
			removeChunk(location.getChunk());
		}

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					if (!addOrRemove && !world.isChunkLoaded(pX+x, pZ+z)) { continue; }
					Chunk chunk = world.getChunkAt(pX+x, pZ+z);
					if (addOrRemove) { addChunk(chunk); } else { removeChunk(chunk); }
				}
			}
		}
	}

	public static void addChunk(Chunk chunk) {
		String location = Utils.locationToString(new Location(chunk.getWorld(), chunk.getX(), 0, chunk.getZ()));

		Integer count = chunks.get(location);
		if (count == null) { count = 0; }
		chunks.put(location, count+1);
		if (count == 0) { chunk.addPluginChunkTicket(Main.plugin); }
	}

	public static void removeChunk(Chunk chunk) {
		String location = Utils.locationToString(new Location(chunk.getWorld(), chunk.getX(), 0, chunk.getZ()));

		Integer count = chunks.get(location);
		if (count == null) { return; }

		if ((count-1) == 0) {
			chunks.remove(location);
			chunk.removePluginChunkTicket(Main.plugin);
			return;
		}

		chunks.put(location, count-1);
	}
}
