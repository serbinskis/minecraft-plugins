package me.wobbychip.smptweaks.custom.chunkloader.loaders;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.Utils;

public class Chunks {
	public static Map<String, Integer> chunks = new HashMap<>();

	public static void markChunks(Location location, int radius, boolean addOrRemove) {
		double pX = location.getChunk().getX();
		double pZ = location.getChunk().getZ();

		if (addOrRemove) {
			addChunk(location.getChunk());
		} else {
			removeChunk(location.getChunk());
		}

		for (double x = -radius; x <= radius; x++) {
			for (double y = -radius; y <= radius; y++) {
				for (double z = -radius; z <= radius; z++) {
					Chunk chunk = location.getWorld().getChunkAt((int) (pX+x), (int) (pZ+z));
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
