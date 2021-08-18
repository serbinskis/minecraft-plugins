package me.wobbychip.discordwhitelist;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static JDA jda;
	public static Guild guild;
	public static String roleID;
	public static List<String> allowed = new ArrayList<String>();

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

		if (System.getenv("WHITELIST_TOKEN") != null) { Main.plugin.getConfig().set("Token", System.getenv("WHITELIST_TOKEN")); }
		if (System.getenv("WHITELIST_GUILD_ID") != null) { Main.plugin.getConfig().set("GuildID", System.getenv("WHITELIST_GUILD_ID")); }
		if (System.getenv("WHITELIST_ROLE_ID") != null) { Main.plugin.getConfig().set("RoleID", System.getenv("WHITELIST_ROLE_ID")); }
		Main.plugin.saveConfig();

		try {
			Utilities.EnableBot();
		} catch (LoginException | InterruptedException e) {
			Utilities.DebugInfo(Utilities.getString("loginException"));
		}

		Bukkit.getPluginManager().registerEvents(new LoginEvent(), Main.plugin);
		Main.plugin.getCommand("dwl").setExecutor(new Commands());
		Utilities.DebugInfo(Utilities.getString("enableMessage"));
	}
}