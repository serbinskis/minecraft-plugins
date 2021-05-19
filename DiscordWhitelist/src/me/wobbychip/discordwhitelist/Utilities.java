package me.wobbychip.discordwhitelist;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.GatewayIntent;

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

	static boolean MemberHasRole(Member member, String roleID) {
		List<Role> roles = member.getRoles();

		for (int i = 0; i < roles.size(); i++) {
			if (roles.get(i).getId().equalsIgnoreCase(roleID)) {
				return true;
			}
		}
		
		return false;
	}

	static boolean PlayerWhitelisted(String playerName) {
		List<Member> members = Main.guild.loadMembers().get();

		for (int i = 0; i < members.size(); i++) {
			if (members.get(i).getEffectiveName().equalsIgnoreCase(playerName)) {
				if (MemberHasRole(members.get(i), Main.roleID)) {
					return true;
				}
			}
		}
		
		return false;
	}

	static void EnableBot() throws LoginException, InterruptedException {
		Main.jda = JDABuilder.createDefault(getString("Token"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).build().awaitReady();
		Main.guild = Main.jda.getGuildById(getString("GuildID"));
		Main.roleID = getString("RoleID");
		Main.jda.addEventListener(new DiscordEvents());
	}
}
