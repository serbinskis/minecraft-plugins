package me.wobbychip.smptweaks.custom.serverpause;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class ServerPause extends CustomTweak {
	public static CustomTweak tweak;
	public static boolean gamerule = true;
	public static int delayTask = -1;

	public static Config config;
	public static int pauseDelay = 0;
	public static boolean quiteCommands = false;

	public ServerPause() {
		super(ServerPause.class.getSimpleName(), false, false);
		ServerPause.tweak = this;
		this.setReloadable(true);
		this.setGameRule("doServerPause", true, true);
		this.setDescription("Pauses server when there are no players online.");
	}

	public void onEnable() {
		loadConfig();

		//Delay event registration so that other plugins can do their thing
		//Tbh, I don't know if this actaully is required, but just in case
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
				if (!ServerPause.canPause()) { return; }
				boolean success = ServerUtils.pauseServer();
				if (success) { Utils.sendMessage("Server is now paused."); }
			}
		}, 20L);
	}

	public void onDisable() {
		ServerUtils.serverShutdown();
	}

	public void onReload() {
		loadConfig();
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(ServerPause.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		ServerPause.config = new Config(configPath, "/tweaks/ServerPause/config.yml");
		ServerPause.pauseDelay = ServerPause.config.getConfig().getInt("pauseDelay");
		ServerPause.quiteCommands  = ServerPause.config.getConfig().getBoolean("quiteCommands");
		if (ServerPause.pauseDelay < 0) { ServerPause.pauseDelay = 0; }

		if (delayTask < 0) { return; }
		delayTask = TaskUtils.rescheduleSyncDelayedTask(delayTask, pauseDelay);
		if (pauseDelay <= 0) { TaskUtils.finishSyncDelayedTask(delayTask); }
	}

	public static boolean canPause() {
		gamerule = ServerUtils.isPaused() ? gamerule : ServerPause.tweak.getGameRuleBoolean(null);
		return (Bukkit.getOnlinePlayers().size() == 0) && gamerule;
	}
}
