package me.wobbychip.smptweaks.custom.serverpause;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	//When player joins, we must resume server
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		ServerUtils.resumeServer();
		ServerPause.logging = true;

		//logging will prevent server from pausing, while player is connecting
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() { ServerPause.logging = false; }
		}, 20L);
	}

	//We also must resume server, when command is being executed
	//BUG - resuming server here will not pause it again
	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerCommandEvent(ServerCommandEvent event) {
		ServerUtils.resumeServer();
		ServerPause.command = true;
		ServerPause.quite = !ServerPause.paused || ServerPause.quiteCommands;
		ServerPause.previous = !ServerPause.paused;

		if (!ServerPause.quite) {
			Utils.sendMessage("Server is now resumed. (Required for commands)");
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() { ServerPause.command = false; }
		}, 1L);
	}
	
}
