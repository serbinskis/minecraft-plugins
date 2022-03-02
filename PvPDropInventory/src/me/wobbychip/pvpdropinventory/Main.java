package me.wobbychip.pvpdropinventory;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public static Plugin plugin;
	public static int timeout;
	public static PlayerTimer timer;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();
		Main.timeout = Main.plugin.getConfig().getInt("PvP_Timeout");
		Main.timer = new PlayerTimer("players.yml");

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
		Utils.sendMessage("&9[PvPDropInventory] PvPDropInventory has loaded!");
	}

	@Override
	public void onDisable() {
		Main.timer.Save(true);
	}
}