package me.wobbychip.ipbind;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

	static void IPBind(CommandSender sender, String playerName) {
		Player player = Bukkit.getPlayer(playerName);

		if (player == null) {
			SendMessage(sender, getString("playerNotFoundMessage"));
			return;
		}

		String section = "players." + player.getUniqueId().toString();
		String IPAddress = player.getAddress().getHostName();
		Main.PlayersConfig.getConfig().set(section, IPAddress);
		Main.PlayersConfig.Save();

		String replacedMessage = getString("bindedAddressMessage").replace("%player%", player.getName());
		SendMessage(sender, replacedMessage);
	}

	@SuppressWarnings("deprecation")
	static void IPUnbind(CommandSender sender, String playerName) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

		if (player == null) {
			SendMessage(sender, getString("playerNotFoundMessage"));
			return;
		}

		String section = "players." + player.getUniqueId().toString();
		Main.PlayersConfig.getConfig().set(section, null);
		Main.PlayersConfig.Save();

		String replacedMessage = getString("unbindedAddressMessage").replace("%player%", player.getName());
		SendMessage(sender, replacedMessage);
	}
}
