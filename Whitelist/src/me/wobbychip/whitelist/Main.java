package me.wobbychip.whitelist;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static Config PlayersConfig;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();
		Main.PlayersConfig = new Config("players.yml");

		Bukkit.getPluginManager().registerEvents(new LoginEvent(), Main.plugin);
		Main.plugin.getCommand("wl").setExecutor(new Commands());

		Utilities.DebugInfo(Utilities.getString("enableMessage"));
	}
}