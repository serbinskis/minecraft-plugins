package me.wobbychip.smptweaks.custom.serverpause;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ServerUtils;

public class ServerPause extends CustomTweak {
	public static boolean stopped = false;

	public ServerPause() {
		super(ServerPause.class.getSimpleName(), true, false);
		this.setDescription("Pauses server when there are no players online.");
	}

	public void onEnable() {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			}
		}, 20L);
	}

	public void onDisable() {
		ServerUtils.resumeServer();
		ServerPause.stopped = true;
	}
}
