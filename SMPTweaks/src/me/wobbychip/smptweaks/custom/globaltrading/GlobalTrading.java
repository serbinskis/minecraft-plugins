package me.wobbychip.smptweaks.custom.globaltrading;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.PaperUtils;
import me.wobbychip.smptweaks.Utils;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class GlobalTrading extends CustomTweak {
	public GlobalTrading() {
		super("GlobalTrading");

		if (this.isEnabled()) {
			if (PaperUtils.isPaper) {
				Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
				this.printEnabled();
			} else {
				Utils.sendMessage("[GlobalTrading] Requires PaperMC.");
			}
		} else {
			this.printDisabled();
		}
	}
}
