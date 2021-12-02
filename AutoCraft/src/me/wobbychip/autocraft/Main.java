package me.wobbychip.autocraft;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.autocraft.crafters.ContainerManager;
import me.wobbychip.autocraft.crafters.CraftInventoryLoader;
import me.wobbychip.autocraft.crafters.MinecartMananger;
import me.wobbychip.autocraft.events.BlockEvents;
import me.wobbychip.autocraft.events.ChunkEvents;
import me.wobbychip.autocraft.events.HopperEvents;
import me.wobbychip.autocraft.events.MinecartEvents;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static boolean dropItem = false;
	public static ContainerManager manager;
	public static MinecartMananger mmanager;

	public static String enableMessage = "&9[AutoCraft] AutoCraft has loaded!";
	public static String versionMessage = "&9[AutoCraft] Server Version: ";
	public static String classErrorMessage = "&9[AutoCraft] Could not load class! - ";

	@Override
	public void onEnable() {
		//Add plugin variable
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

		//Load classes and create new managers
		if (!ReflectionUtils.loadClasses()) { return; }
		manager = new ContainerManager();
		mmanager = new MinecartMananger();

		//Register events
		Bukkit.getPluginManager().registerEvents(new BlockEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new ChunkEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new MinecartEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new HopperEvents(), Main.plugin);
		Utils.sendMessage(enableMessage);

		//Load inventories
		for (World world : Bukkit.getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				Map<Location, List<ItemStack>> itemMap = CraftInventoryLoader.loadChunk(getSaveFolder(), chunk.getWorld(), chunk.getX(), chunk.getZ());
				for (Location location : itemMap.keySet()) {
					manager.load(location, itemMap.get(location));
				}
			}
		}
	}

	@Override
	public void onDisable() {
		//Stop the player from duplicating items on server reload
		for (Player player : Bukkit.getOnlinePlayers()) {
			InventoryView view = player.getOpenInventory();
			if (view == null || manager.getLocation(view.getTopInventory()) == null) {
				continue;
			}
			player.closeInventory();
		}

		//Save inventories
		for (World world : Bukkit.getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				manager.unload(chunk);
			}
		}
	}

	public File getSaveFolder() {
		return new File(getDataFolder() + "/" + "saves");
	}
}