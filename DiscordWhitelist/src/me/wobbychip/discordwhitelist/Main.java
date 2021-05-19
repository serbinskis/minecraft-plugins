package me.wobbychip.discordwhitelist;

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

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

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