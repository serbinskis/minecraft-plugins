package me.wobbychip.expbottles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public static Plugin plugin;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Bukkit.getServer().getPluginManager().registerEvents(new Events(), Main.plugin);
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[ExpBottles] ExpBottles has loaded!"));
	}
}