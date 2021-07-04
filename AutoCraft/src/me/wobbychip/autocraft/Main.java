package me.wobbychip.autocraft;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.autocraft.events.BlockEvents;
import me.wobbychip.autocraft.events.InventoryEvents;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static HashMap<String, InventoryManager> inventoryManagers = new HashMap<String, InventoryManager>();
	public static final String Delimiter = "#";

	@Override
	public void onEnable() {
		//Add plugin variable
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

		//Register events
		Bukkit.getPluginManager().registerEvents(new BlockEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Utilities.DebugInfo(this.getConfig().getString("enableMessage"));
	}
}