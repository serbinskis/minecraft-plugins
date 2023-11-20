package me.wobbychip.smptweaks.custom.serverpause;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.events.ServerConnectionEvent;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	public int isConnecting = -1;
	public int connectingTask = -1;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onServerConnectionEvent(ServerConnectionEvent event) {
		if (event.getConnections() > 0) { return; }

		//We also must stop already running task if there is such,
		//player can rejoin and leave as many times as they want
		//and this will trigger this event
		if (ServerPause.delayTask > -1) { TaskUtils.cancelSyncDelayedTask(ServerPause.delayTask); }

		ServerPause.delayTask = TaskUtils.scheduleSyncDelayedTask(() -> {
            int delayTask = ServerPause.delayTask;
            ServerPause.delayTask = -1;
            if (!ServerPause.canPause(false)) { return; }

            //Save worlds before pausing server, since when server is paused
            //worlds cannot be saved
            if (!ServerUtils.isPaused()) {
                for (World world: Bukkit.getWorlds()) { world.save(); }
            }

            //If AsyncPlayerPreLoginEvent executed and someone is connecting, then we
            //cannot pause the server and this task should be rescheduled
            if (isConnecting > -1) { TaskUtils.rescheduleSyncDelayedTask(delayTask, ServerPause.pauseDelay); }
            if (isConnecting > -1) { return; }

            boolean success = ServerUtils.pauseServer();
            if (success) { Utils.sendMessage("Server is now paused. (Worlds saved)"); }
        }, ServerPause.pauseDelay);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		//1, because at the moment when player leaves, he still is online
		//and we need this code only to run when last player leaves
		if (Bukkit.getOnlinePlayers().size() > 1) { return; }
		onServerConnectionEvent(new ServerConnectionEvent(0, false));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		//Since this is async event, which means it can run concurrently with
		//PlayerQuitEvent runnable, we set isConnecting to true, but revert it in
		//sync with another runnable, because runnables are synchronized
		if (isConnecting > -1) { TaskUtils.cancelSyncDelayedTask(isConnecting); }
		isConnecting = TaskUtils.scheduleSyncDelayedTask(() -> isConnecting = -1, 1);

		//Resume server if player is trying to connect to the server
		boolean success = ServerUtils.resumeServer();
		if (success) { Utils.sendMessage("Server is now resumed."); }
		if (connectingTask > -1) { TaskUtils.cancelSyncDelayedTask(connectingTask); }

		//Check if something happened and player did not get into server
		//Banned, whitelist, connection timeout, etc
		//ERROR: if player takes longer than 5s to connect it will throw an error
		connectingTask = TaskUtils.scheduleSyncDelayedTask(() -> {
            connectingTask = -1;
            if (ServerPause.delayTask > -1) { return; } //If there is delay task then we don't need to pasue server again
            if (!ServerPause.canPause(true)) { return; }
            boolean success1 = ServerUtils.pauseServer();
            if (success1) { Utils.sendMessage("Server is now paused."); }
        }, 20L*ServerPause.CONNECTION_MAX_TIME);
	}

	//We also must resume server, when command is being executed
	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerCommandEvent(ServerCommandEvent event) {
		boolean success = ServerUtils.resumeServer();
		if (!success) { return; }

		if (!ServerPause.quiteCommands) {
			Utils.sendMessage("Server is now resumed. (Required for commands)");
		}

		//This will set server back to pause in the beggining of the next tick, after all events
		//so that it does not affect commands, because, for example, when stopping server,
		//the server must be unpaused, which means that when there is console command,
		//server must be resumed for 1 tick, this does not affect player commands, since
		//when players are online server is always resumed
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				if (ServerPause.delayTask > -1) { return; }
				if (!ServerPause.canPause(false)) { return; }
				boolean success = ServerUtils.pauseServer();
				if (success && !ServerPause.quiteCommands) { Utils.sendMessage("Server is now paused."); }
			}
		}, 0L);
	}
}
