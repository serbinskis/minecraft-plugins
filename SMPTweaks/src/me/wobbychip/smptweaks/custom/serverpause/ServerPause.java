package me.wobbychip.smptweaks.custom.serverpause;

import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class ServerPause extends CustomTweak {
	public static CustomTweak tweak;
	public static boolean gamerule = true;
	public static int delayTask = -1;

	public static int pauseDelay = 0;
	public static boolean quiteCommands = false;

	public ServerPause() {
		super(ServerPause.class, false, false);
		ServerPause.tweak = this;
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doServerPause", true, true);
		this.setReloadable(true);
		this.setDescription("Pauses the server when there are no players online.");
	}

	public void onEnable() {
		this.onReload();

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
		ServerPause.pauseDelay = this.getConfig(0).getConfig().getInt("pauseDelay");
		ServerPause.quiteCommands = this.getConfig(0).getConfig().getBoolean("quiteCommands");
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
