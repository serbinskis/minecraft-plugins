package me.wobbychip.discordwhitelist;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.discordwhitelist.events.DiscordEvents;
import me.wobbychip.discordwhitelist.events.LoginEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static JDA jda;
	public static Guild guild;
	public static String roleID;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

		if (System.getenv("WHITELIST_TOKEN") != null) { Main.plugin.getConfig().set("Token", System.getenv("WHITELIST_TOKEN")); }
		if (System.getenv("WHITELIST_GUILD_ID") != null) { Main.plugin.getConfig().set("GuildID", System.getenv("WHITELIST_GUILD_ID")); }
		if (System.getenv("WHITELIST_ROLE_ID") != null) { Main.plugin.getConfig().set("RoleID", System.getenv("WHITELIST_ROLE_ID")); }
		Main.plugin.saveConfig();

		try {
			enableBot();
		} catch (LoginException | InterruptedException e) {
			Utils.sendMessage(Utils.getString("loginException"));
		}

		Bukkit.getPluginManager().registerEvents(new LoginEvent(), Main.plugin);
		Main.plugin.getCommand("dwl").setExecutor(new Commands());
		Utils.sendMessage(Utils.getString("enableMessage"));
	}

	@Override
	public void onDisable() {
		if (Main.jda != null) {
			Main.jda.shutdownNow();
		}
	}

	static boolean enableBot() throws LoginException, InterruptedException {
		Main.guild = null;
		Main.jda = JDABuilder.createDefault(Utils.getString("Token"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).build().awaitReady();
		Main.guild = Main.jda.getGuildById(Utils.getString("GuildID"));

		if (Main.guild == null) {
			Utils.sendMessage(Utils.getString("guildException"));
			return false;
		}

		Main.roleID = Utils.getString("RoleID");
		Main.jda.addEventListener(new DiscordEvents());
		return true;
	}
}