package me.wobbychip.discordwhitelist.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.wobbychip.discordwhitelist.Main;
import me.wobbychip.discordwhitelist.Utils;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordEvents extends ListenerAdapter {
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		if (!Main.plugin.getConfig().getBoolean("Enabled")) { return; }
		if (!event.getGuild().getId().equalsIgnoreCase(Main.guild.getId())) { return; }
		if (!event.getRoles().get(0).getId().equalsIgnoreCase(Main.roleID)) { return; }

		Player player = Bukkit.getServer().getPlayer(event.getMember().getEffectiveName());
		if (Utils.checkPermissions(player, "dwl.bypass")) { return; }

		//This fixes error: Asynchronous player kick!
		Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
			public void run() {
				String message = (Utils.getString("kickMessage") + Utils.getString("secondReason")).replaceAll("%n", "\n");
				player.kickPlayer(ChatColor.translateAlternateColorCodes('&', message));
			}
		});
	}
}
