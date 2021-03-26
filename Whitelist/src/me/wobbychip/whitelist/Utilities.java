package me.wobbychip.whitelist;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utilities {
	static String getString(String arg0) {
		return Main.plugin.getConfig().getString(arg0);
	}

	public static void DebugInfo(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	static void SendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	static boolean CheckPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission(permission)) {
				SendMessage(sender, getString("permissionMessage"));
				return false;
			}
		}

		return true;
	}

	static boolean PlayerWhitelisted(String playerName) {
		List<String> players = Main.PlayersConfig.getConfig().getStringList("players");

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).equalsIgnoreCase(playerName)) {
				return true;
			}
		}
		
		return false;
	}
}
