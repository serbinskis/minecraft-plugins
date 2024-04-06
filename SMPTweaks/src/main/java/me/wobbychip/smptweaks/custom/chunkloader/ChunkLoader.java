package me.wobbychip.smptweaks.custom.chunkloader;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.chunkloader.events.PlayerEvents;
import me.wobbychip.smptweaks.custom.chunkloader.events.PotionEvents;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.Border;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.FakePlayer;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.LoaderBlock;
import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ChunkLoader extends CustomTweak {
	public static String isAggravator = "isAggravator";
	public static int viewDistance = Bukkit.getViewDistance();
	public static boolean enableAggravator = false;
	public static boolean highlighting = true;
	public static CustomTweak tweak;
	private int task;

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
		Bukkit.getPluginManager().registerEvents(new PlayerEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
		CustomBlocks.registerBlock(new LoaderBlock());

		this.task = TaskUtils.scheduleSyncRepeatingTask(() -> {
			if (ServerUtils.isPaused()) { return; }
			Border.update();
			FakePlayer.update();
		}, 1L, 5L);

		for (String loader : this.getConfig(1).getConfig().getStringList("chunkloaders")) {
			Location location = Utils.stringToLocation(loader);
			String message = String.format("Loading at %s: X: %s Y: %s Z: %s", location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
			ChunkLoader.tweak.printMessage(message, true);
			if (!location.getBlock().getChunk().isLoaded()) { location.getBlock().getChunk().load(); }
		}
	}

	public void onReload() {
		FileConfiguration config = this.getConfig(0).getConfig();
		if (!config.getBoolean("useServerViewDistance")) { ChunkLoader.viewDistance = config.getInt("viewDistance"); }
		ChunkLoader.enableAggravator = config.getBoolean("enableAggravator");
		ChunkLoader.highlighting = config.getBoolean("highlighting");
	}

	public void onDisable() {
		TaskUtils.cancelTask(this.task);
		LoaderBlock.saveAll();
	}
}
