package me.wobbychip.discordwhitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordEvents extends ListenerAdapter {
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		if (!Main.plugin.getConfig().getBoolean("Enabled")) { return; }
		if (!event.getGuild().getId().equalsIgnoreCase(Main.guild.getId())) { return; }
		if (!event.getRoles().get(0).getId().equalsIgnoreCase(Main.roleID)) { return; }

		Player player = Bukkit.getServer().getPlayer(event.getMember().getEffectiveName());
		if (Utilities.CheckPermissions(player, "dwl.bypass")) { return; }

		if (player != null) {
    		Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
    			public void run() {
    				String replacedMessage = Utilities.getString("kickMessage").replaceAll("%n", "\n");
    				player.kickPlayer(ChatColor.translateAlternateColorCodes('&', replacedMessage));
    			}
    		});
		}
	}
}
