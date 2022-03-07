package me.wobbychip.smptweaks.custom.expbottles;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class ExpBottles extends CustomTweak {
	public ExpBottles() {
		super("ExpBottles");

		if (this.isEnabled()) {
			Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			this.printEnabled();
		} else {
			this.printDisabled();
		}
	}
}
