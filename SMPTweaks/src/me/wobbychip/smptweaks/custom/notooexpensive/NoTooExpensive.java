package me.wobbychip.smptweaks.custom.notooexpensive;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class NoTooExpensive extends CustomTweak {
	public NoTooExpensive() {
		super("NoTooExpensive");

		if (this.isEnabled()) {
			Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			this.printEnabled();
		} else {
			this.printDisabled();
		}
	}
}
