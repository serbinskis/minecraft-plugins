package me.wobbychip.smptweaks.custom.chunkloader;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.chunkloader.events.PlayerEvents;
import me.wobbychip.smptweaks.custom.chunkloader.events.PotionEvents;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.Border;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.FakePlayer;
import me.wobbychip.smptweaks.custom.chunkloader.block.LoaderBlock;
import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ChunkLoader extends CustomTweak {
	public static Player fakePlayer;
	public static int viewDistance = Bukkit.getViewDistance();
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

		ChunkLoader.fakePlayer = ReflectionUtils.addFakePlayer(new Location(Bukkit.getWorlds().get(0), 0, 0, 0), UUID.randomUUID(), false, true, true);
		Boolean gameRuleValue = Bukkit.getWorlds().get(0).getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS);
		Bukkit.getWorlds().get(0).setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> Utils.grantAdvancemnt(fakePlayer, advancement));
		Bukkit.getWorlds().get(0).setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, gameRuleValue);

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
		ChunkLoader.highlighting = config.getBoolean("highlighting");
	}

	public void onDisable() {
		TaskUtils.cancelTask(this.task);
		LoaderBlock.saveAll();
	}
}
