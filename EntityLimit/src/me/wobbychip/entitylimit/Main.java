package me.wobbychip.entitylimit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static List<String> excludeReason = new ArrayList<>();
	public static Boolean pluginEnabled = false;
	public static int maximumDistance = 0;
	public static int Limit = 0;

	public static void loadConfig() {
		excludeReason = Main.plugin.getConfig().getStringList("excludeReason");
		pluginEnabled = Main.plugin.getConfig().getBoolean("Enabled");
		maximumDistance = Main.plugin.getConfig().getInt("maximumDistance");
		Limit = Main.plugin.getConfig().getInt("Limit");
	}

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();
		loadConfig();

		Main.plugin.getCommand("entitylimit").setExecutor(new Commands());
		Bukkit.getServer().getPluginManager().registerEvents(new Events(), Main.plugin);
		Utilities.SendMessage(Bukkit.getConsoleSender(), Utilities.getString("enableMessage"));
	}
}