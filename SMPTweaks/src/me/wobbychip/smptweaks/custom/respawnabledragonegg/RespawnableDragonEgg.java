package me.wobbychip.smptweaks.custom.respawnabledragonegg;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class RespawnableDragonEgg extends CustomTweak {
	public RespawnableDragonEgg() {
		super(RespawnableDragonEgg.class.getSimpleName(), false, false);
		this.setDescription("Respawns ender dragon egg on each death.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
