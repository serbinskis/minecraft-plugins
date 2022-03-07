package me.wobbychip.smptweaks.custom.respawnabledragonegg;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class RespawnableDragonEgg extends CustomTweak {
	public RespawnableDragonEgg() {
		super("RespawnableDragonEgg");

		if (this.isEnabled()) {
			Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			this.printEnabled();
		} else {
			this.printDisabled();
		}
	}
}
