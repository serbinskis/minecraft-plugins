package me.serbinskis.smptweaks.custom.respawnabledragonegg;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;

public class RespawnableDragonEgg extends CustomTweak {
	public RespawnableDragonEgg() {
		super(RespawnableDragonEgg.class, false, false);
		this.setDescription("Respawns ender dragon egg on each death.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
