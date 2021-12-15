package me.wobbychip.discordwhitelist;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Utils {
	public static String getString(String arg0) {
		return Main.plugin.getConfig().getString(arg0);
	}

	public static void sendMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static boolean checkPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission(permission)) {
				sendMessage(sender, getString("permissionMessage"));
				return false;
			}
		}

		return true;
	}

	public static boolean hasRole(Member member, String roleID) {
		List<Role> roles = member.getRoles();

		for (int i = 0; i < roles.size(); i++) {
			if (roles.get(i).getId().equalsIgnoreCase(roleID)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isPlayerWhitelisted(String name) {
		if (Main.guild == null) {
			Utils.sendMessage(getString("guildException"));
			return false;
		}

		List<Member> members = Main.guild.loadMembers().get();

		for (int i = 0; i < members.size(); i++) {
			if (members.get(i).getEffectiveName().equalsIgnoreCase(name)) {
				if (hasRole(members.get(i), Main.roleID)) {
					return true;
				}
			}
		}

		return false;
	}
}
