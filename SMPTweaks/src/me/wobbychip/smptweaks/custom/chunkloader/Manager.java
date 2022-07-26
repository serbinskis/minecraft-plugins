package me.wobbychip.smptweaks.custom.chunkloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;

public class Manager {
	public static final String delimiter = "#";
	public Config config;
	protected Map<String, Loader> loaders = new HashMap<>();

	public Manager(Config config) {
		this.config = config;

		for (String location : config.getConfig().getStringList("chunkloaders")) {
			addLoader(stringToLocation(location).getBlock(), false);
		}

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				updateAll();
			}
		}, 20L, 5L);
	}

	public Border getBorder(Player player) {
		for (Loader loader : loaders.values()) {
			if (loader.getBorder().containsPlayer(player)) { return loader.getBorder(); }
		}

		return null;
	}

	public void addLoader(Block block, boolean doSave) {
		if (block.getType() != Material.LODESTONE) { return; }
		String location = locationToString(block.getLocation());
		if (loaders.containsKey(location)) { return; }
		if (!block.getChunk().isLoaded()) { block.getChunk().load(); }
		loaders.put(location, new Loader(block));
		if (doSave) { saveAll(); }
	}

	public void removeLoader(Block block) {
		String location = locationToString(block.getLocation());
		if (!loaders.containsKey(location)) { return; }
		loaders.remove(location).remove(false);
		saveAll();
	}

	public Loader getLoader(Block block) {
		String location = locationToString(block.getLocation());
		if (loaders.containsKey(location)) { return loaders.get(location); }
		return null;
	}

	public void updateLoader(Block block) {
		if (block.getType() != Material.LODESTONE) { return; }
		String location = locationToString(block.getLocation());
		if (loaders.containsKey(location)) { loaders.get(location).update(false); }
	}

	public void updateAll() {
		List<Block> remove = new ArrayList<>();
		
		for (Loader loader : loaders.values()) {
			if (!loader.isLoader()) { remove.add(loader.getLocation().getBlock()); }
		}

		for (Block block : remove) {
			removeLoader(block);
		}
	}

	public void disableAll() {		
		for (Loader loader : loaders.values()) {
			loader.remove(true);
		}
	}

	public void saveAll() {
		config.getConfig().set("chunkloaders", new ArrayList<>(loaders.keySet()));
		config.Save();
	}

	public static String locationToString(Location location) {
		return location.getWorld().getName() + delimiter + String.valueOf(location.getX()) + delimiter + String.valueOf(location.getY()) + delimiter +  String.valueOf(location.getZ());
	}

	public static Location stringToLocation(String location) {
		String[] splited = location.split(delimiter, 0);
		return new Location(Bukkit.getWorld(splited[0]), Double.parseDouble(splited[1]), Double.parseDouble(splited[2]), Double.parseDouble(splited[3]));
	}
}
