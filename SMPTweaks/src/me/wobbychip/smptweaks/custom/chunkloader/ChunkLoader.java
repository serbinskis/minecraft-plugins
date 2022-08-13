package me.wobbychip.smptweaks.custom.chunkloader;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.chunkloader.events.BlockEvents;
import me.wobbychip.smptweaks.custom.chunkloader.events.EntityEvents;
import me.wobbychip.smptweaks.custom.chunkloader.events.PlayerEvents;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.Chunks;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.Manager;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class ChunkLoader extends CustomTweak {
	public static String isChunkLoader = "isChunkLoader";
	public static String isAggravator = "isAggravator";
	public static int viewDistance = Bukkit.getViewDistance();
	public static int simulationDistance = Bukkit.getSimulationDistance()*16;
	public static boolean enableAggravator = false;
	public static ChunkLoader tweak;
	public static Config loaders;
	public static Manager manager;
	public static Chunks chunks;

	public ChunkLoader() {
		super(ChunkLoader.class.getSimpleName(), false);
	}

	public void onEnable() {
		loadConfig();
		ChunkLoader.tweak = this;
		ChunkLoader.chunks = new Chunks();
		ChunkLoader.manager = new Manager(loaders);

		Bukkit.getPluginManager().registerEvents(new BlockEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new EntityEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new PlayerEvents(), Main.plugin);
	}

	public void onDisable() {
		manager.onDisable();
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(ChunkLoader.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/loaders.yml";
		ChunkLoader.loaders = new Config(configPath, "/tweaks/ChunkLoader/loaders.yml");

		configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		FileConfiguration config = new Config(configPath, "/tweaks/ChunkLoader/config.yml").getConfig();
		if (!config.getBoolean("useServerViewDistance")) { ChunkLoader.viewDistance = config.getInt("viewDistance"); }
		ChunkLoader.enableAggravator = config.getBoolean("enableAggravator");
	}
}
