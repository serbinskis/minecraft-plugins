package me.wobbychip.smptweaks.custom.shriekercansummon;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class ShriekerCanSummon extends CustomTweak {
	public ShriekerCanSummon() {
		super("ShriekerCanSummon");

		if (this.isEnabled()) {
			Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			this.printEnabled();
		} else {
			this.printDisabled();
		}
	}
}
