package me.wobbychip.smptweaks.custom.chunkloader;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class ChunkLoader extends CustomTweak {
	public static final String isChunkLoader = "isChunkLoader";
	public static Config config;
	public static Manager manager;

	public ChunkLoader() {
		super("ChunkLoader");

		if (this.isEnabled()) {
			loadConfig();
			manager = new Manager(config);
			Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			this.printEnabled();
		} else {
			this.printDisabled();
		}
	}

	public void onDisable() {
		manager.disableAll();
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(ChunkLoader.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/loaders.yml";
		ChunkLoader.config = new Config(configPath, "/tweaks/ChunkLoader/loaders.yml");
	}
}
