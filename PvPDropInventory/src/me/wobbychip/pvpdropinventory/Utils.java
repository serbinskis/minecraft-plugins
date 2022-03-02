package me.wobbychip.pvpdropinventory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Utils {
	public static void sendMessage(String arg0) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', arg0));
	}
}
