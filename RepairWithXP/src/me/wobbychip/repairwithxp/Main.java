package me.wobbychip.repairwithxp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public static Main plugin;
	public static int TaskID = 0;
	public static int perTicks = 5;
	public static int perXP = 5;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

		StartPlugin();
		Main.plugin.getCommand("repairwithxp").setExecutor(new Commands());
		Utilities.SendMessage(Bukkit.getConsoleSender(), Utilities.getString("enableMessage"));
	}

	public static void StartPlugin() {
		if (TaskID != 0) {
			Bukkit.getServer().getScheduler().cancelTask(TaskID);
			TaskID = 0;
		}

		perTicks = Main.plugin.getConfig().getInt("perTicks");
		perXP = Main.plugin.getConfig().getInt("perXP");

		TaskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable(){
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                	Utilities.checkPlayer(player);
                }
            }
        }, 0L, perTicks);
	}
}