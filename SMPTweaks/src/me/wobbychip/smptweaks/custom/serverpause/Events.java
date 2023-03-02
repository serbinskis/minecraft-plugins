package me.wobbychip.smptweaks.custom.serverpause;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		//1, because at the moment when player leaves, he still is online
		//and we need this code only to run when last playe leaves
		if (Bukkit.getOnlinePlayers().size() > 1) { return; }

		//We also must stop already running task if there is such,
		//player can rejoin and leave as many times as they want
		//and this will trigger this event
		if (ServerPause.delayTask > -1) { TaskUtils.cancelSyncDelayedTask(ServerPause.delayTask); }

		ServerPause.delayTask = TaskUtils.scheduleSyncDelayedTask(new Runnable() {
			public void run() {
				ServerPause.delayTask = -1;
				if (!ServerPause.canPause()) { return; }
				boolean success = ServerUtils.pauseServer();
				if (success) { Utils.sendMessage("Server is now paused."); }
			}
		}, ServerPause.pauseDelay);
	}

	//When player joins, we must resume server
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		boolean success = ServerUtils.resumeServer();
		if (success) { Utils.sendMessage("Server is now resumed."); }

		//Check if something happened and player did not get into server
		//Banned, whitelist, connection timeout, etc
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				//If there is delay task then we don't need to pasue server again
				if (ServerPause.delayTask > -1) { return; }
				if (!ServerPause.canPause()) { return; }
				boolean success = ServerUtils.pauseServer();
				if (success) { Utils.sendMessage("Server is now paused."); }
			}
		}, 20L);
	}

	//We also must resume server, when command is being executed
	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerCommandEvent(ServerCommandEvent event) {
		boolean success = ServerUtils.resumeServer();
		if (!success) { return; }

		if (!ServerPause.quiteCommands) {
			Utils.sendMessage("Server is now resumed. (Required for commands)");
		}

		//This will set server back to pause in the same tick, but after all events
		//so that it does not affect commands, because, for example, when stopping server,
		//the server must be unpaused, which means that when there is console command,
		//server must be resumed for 1 tick (current tick), this does not affect player
		//commands, since when players are online server is always resumed
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				if (ServerPause.delayTask > -1) { return; }
				if (!ServerPause.canPause()) { return; }
				boolean success = ServerUtils.pauseServer();
				if (success && !ServerPause.quiteCommands) { Utils.sendMessage("Server is now paused."); }
			}
		}, 0L);
	}
}
