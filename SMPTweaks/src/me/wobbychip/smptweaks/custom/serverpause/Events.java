package me.wobbychip.smptweaks.custom.serverpause;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	public boolean quite = false;
	public boolean logging = false;
	public boolean command = false;
	public boolean paused = true;
	public boolean previous = !paused;

	//Pause server if there are no players online
	/*@EventHandler(priority = EventPriority.MONITOR)
	public void onServerTickStartEvent(ServerTickStartEvent event) {
		paused = (Bukkit.getOnlinePlayers().size() <= 0);

		if ((previous != paused) && paused && !quite) { Utils.sendMessage("Server is now paused."); }
		if ((previous != paused) && !paused && !quite) { Utils.sendMessage("Server is now resumed."); }
		previous = paused;

		if (logging || command || !paused || ServerPause.stopped) { return; }
		ServerUtils.pauseServer();
	}*/

	/*@EventHandler(priority = EventPriority.MONITOR)
	public void onServerTickEndEvent(ServerTickEndEvent event) {
		ServerUtils.resumeServer();
	}*/

	//Pause server if there are no players online
	//When player leaves the counter will not update immediately
	//so make the runable that will run in the same tick, but
	//because it is runnable it will run after the events
	//Also this will allow other events to run before pausing the server
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				paused = (Bukkit.getOnlinePlayers().size() <= 0);

				if ((previous != paused) && paused && !quite) { Utils.sendMessage("Server is now paused."); }
				if ((previous != paused) && !paused && !quite) { Utils.sendMessage("Server is now resumed."); }
				previous = paused;

				if (logging || command || !paused || ServerPause.stopped) { return; }
				ServerUtils.pauseServer();
			}
		}, 0L);
	}

	//When player joins, we must resume server
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		logging = true;
		ServerUtils.resumeServer();

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				logging = false;
			}
		}, 20L);
	}

	//We also must resume server, when command is being executed
	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerCommandEvent(ServerCommandEvent event) {
		ServerUtils.resumeServer();
		command = true;
		quite = true;

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				quite = false;
				command = false;
			}
		}, 1L);
	}
	
}
