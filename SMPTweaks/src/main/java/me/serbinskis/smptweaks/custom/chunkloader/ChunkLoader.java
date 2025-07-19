package me.serbinskis.smptweaks.custom.chunkloader;

import me.serbinskis.smptweaks.custom.chunkloader.block.LoaderBlock;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.chunkloader.events.PlayerEvents;
import me.serbinskis.smptweaks.custom.chunkloader.loaders.Border;
import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.ServerUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ChunkLoader extends CustomTweak {
	public static int viewDistance = Bukkit.getViewDistance();
	public static boolean highlighting = true;
	public static CustomTweak tweak;
	private int task;

	public ChunkLoader() {
		super(ChunkLoader.class, true, false);
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
		CustomBlocks.registerBlock(new LoaderBlock());

		this.task = TaskUtils.scheduleSyncRepeatingTask(() -> {
			if (ServerUtils.isPaused()) { return; }
			Border.update();
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
		ChunkLoader.highlighting = config.getBoolean("highlighting");
	}

	public void onDisable() {
		TaskUtils.cancelTask(this.task);
		LoaderBlock.saveAll();
	}
}
