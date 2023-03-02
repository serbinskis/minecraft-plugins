package me.wobbychip.smptweaks.custom.serverpause;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class ServerPause extends CustomTweak {
	public static boolean gamerule = true;
	public static boolean stopped = false;
	public static boolean quite = false;
	public static boolean logging = false;
	public static boolean command = false;
	public static boolean paused = true;
	public static boolean previous = !paused;
	public static int delayTask = -1;

	public static Config config;
	public static int pauseDelay = 0;
	public static boolean quiteCommands = false;
	public static Runnable runnable = null;;

	public ServerPause() {
		super(ServerPause.class.getSimpleName(), false, false);
		this.setReloadable(true);
		this.setGameRule("doServerPause", true, true);
		this.setDescription("Pauses server when there are no players online.");
	}

	public void onEnable() {
		loadConfig();

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			}
		}, 20L);

		//Create runnable that will check for changes and pause or resume server
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() { checkServer(); }
		}, 20L, 1L);
	}

	public void onDisable() {
		ServerUtils.resumeServer();
		ServerPause.stopped = true;
	}

	public void onReload() {
		loadConfig();
	}

	//[22:32:56 INFO]: just run
	//[22:32:56 INFO]: Server is now paused.
	//[22:32:56 INFO]: Reloaded ServerPause.
	//[22:32:58 INFO]: Server is now paused.

	public static void loadConfig() {
		List<String> list = Arrays.asList(ServerPause.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		ServerPause.config = new Config(configPath, "/tweaks/ServerPause/config.yml");
		ServerPause.pauseDelay = ServerPause.config.getConfig().getInt("pauseDelay");
		ServerPause.quiteCommands  = ServerPause.config.getConfig().getBoolean("quiteCommands");

		//If there is running task cancel it
		if (delayTask > -1) { Bukkit.getServer().getScheduler().cancelTask(delayTask); }

		//This will fix issue: resume message not showing when player joining
		//Remember quit is set in the onServerCommandEvent
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() { quite = false; }
		}, 1L);

		//If there was a task, delay it again or run now
		if ((pauseDelay > 0) && (runnable != null)) {
			quite = false;
			delayTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, runnable, pauseDelay);
		} else {
			if (runnable != null) { runnable.run(); }
		}
	}

	public void checkServer() {
		if (logging || command || stopped) { return; }

		//Check if server should be paused, and if it can be paused
		//Gamerule cannot be obtained if server is paused, so we either 
		//use last saved or get current in case if server is not paused
		//For first time server is not gonna be paused so we can obtain
		//and save that value

		//Then if previous state is different update it with current
		//otherwise just return, since nothing changed
		gamerule = ServerUtils.isPaused() ? gamerule : this.getGameRuleBoolean(null);
		paused = (Bukkit.getOnlinePlayers().size() <= 0) && gamerule;
		if (previous == paused) { return; } else { previous = paused; }

		//If there was a delayed task then cancel it
		boolean isTask = (delayTask > -1);
		if (isTask) { Bukkit.getServer().getScheduler().cancelTask(delayTask); }

		//Save current quite and reset it, quite is only for once
		//Also it is saved so that it could be used in delayed task
		boolean curr_quite = quite;
		if (quite) { quite = false; }

		//Server resume is never delayed
		if (!paused && !isTask && !curr_quite) { Utils.sendMessage("Server is now resumed."); }
		if (!paused) { return; }

		//Create runnable for dealy
		runnable = new Runnable() { public void run() {
			delayTask = -1;
			runnable = null;
			if (paused && !curr_quite) { Utils.sendMessage("Server is now paused."); }
			ServerUtils.pauseServer();
		}};

		//If there is delay then start delayed task, otherwise just run runnable
		if (pauseDelay <= 0) {
			runnable.run();
		} else {
			delayTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, runnable, pauseDelay);
		}
	}
}
