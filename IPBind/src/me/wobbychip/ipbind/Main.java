package me.wobbychip.ipbind;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public static Main plugin;
	public static Config PlayersConfig;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();
		Main.PlayersConfig = new Config("players.yml");

		Bukkit.getPluginManager().registerEvents(new LoginEvent(), Main.plugin);
		Main.plugin.getCommand("ipbind").setExecutor(new Commands());
		Main.plugin.getCommand("ipunbind").setExecutor(new Commands());

		Utilities.DebugInfo(Utilities.getString("enableMessage"));
	}
}