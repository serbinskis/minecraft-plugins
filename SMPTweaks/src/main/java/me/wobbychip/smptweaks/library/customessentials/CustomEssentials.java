package me.wobbychip.smptweaks.library.customessentials;

import me.wobbychip.smptweaks.Main;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class CustomEssentials {
	public static void start() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		if (plugin == null) { return; }
		Bukkit.getPluginManager().registerEvents(new me.wobbychip.smptweaks.library.customessentials.events.PlayerEvents(plugin), Main.plugin);
	}
}
