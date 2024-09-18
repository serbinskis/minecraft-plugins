package me.serbinskis.smptweaks.library.placeholderapi;

import org.bukkit.Bukkit;

public class PlaceholderAPI {
	public static void register() {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) { return; }
		new CustomExpansion().register();
    }
}
