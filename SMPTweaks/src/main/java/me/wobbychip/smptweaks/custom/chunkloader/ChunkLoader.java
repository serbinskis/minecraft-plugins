package me.wobbychip.smptweaks.custom.chunkloader;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.chunkloader.events.BlockEvents;
import me.wobbychip.smptweaks.custom.chunkloader.events.EntityEvents;
import me.wobbychip.smptweaks.custom.chunkloader.events.PlayerEvents;
import me.wobbychip.smptweaks.custom.chunkloader.events.PotionEvents;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.Manager;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ChunkLoader extends CustomTweak {
	public static String isChunkLoader = "isChunkLoader";
	public static String isAggravator = "isAggravator";
	public static int viewDistance = Bukkit.getViewDistance();
	public static int simulationDistance = Bukkit.getSimulationDistance()*16;
	public static boolean enableAggravator = false;
	public static boolean highlighting = true;
	public static CustomTweak tweak;
	public static Manager manager;

	public ChunkLoader() {
		super(ChunkLoader.class, false, false);
		this.setConfigs(List.of("config.yml", "loaders.yml"));
		this.setGameRule("doChunkLoaders", true, false);
		this.setDescription("Allows loading chunks as if a player was standing there. " +
							"Crops do grow and mobs also spawn. " +
							"Put on top of a lodestone item frame with nether star. " +
							"Power lodestone with redstone and enjoy.");
		ChunkLoader.tweak = this;
	}

	public void onEnable() {
		this.onReload();
		ChunkLoader.manager = new Manager(this.getConfig(1));

		Bukkit.getPluginManager().registerEvents(new BlockEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new EntityEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new PlayerEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
	}

	public void onReload() {
		FileConfiguration config = this.getConfig(0).getConfig();
		if (!config.getBoolean("useServerViewDistance")) { ChunkLoader.viewDistance = config.getInt("viewDistance"); }
		ChunkLoader.enableAggravator = config.getBoolean("enableAggravator");
		ChunkLoader.highlighting = config.getBoolean("highlighting");
	}

	public void onDisable() {
		manager.onDisable();
	}
}
