package me.wobbychip.workbenchinventory;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.workbenchinventory.events.InventoryEvents;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;

	@Override
	public void onEnable() {
		//Add plugin variable
		Main.plugin = this;

		//Register events
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Utilities.DebugInfo("&9[WorkbenchInventory] WorkbenchInventory has loaded!");
	}
}