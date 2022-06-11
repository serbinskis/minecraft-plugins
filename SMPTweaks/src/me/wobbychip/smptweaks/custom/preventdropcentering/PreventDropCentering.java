package me.wobbychip.smptweaks.custom.preventdropcentering;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.PaperUtils;
import me.wobbychip.smptweaks.Utils;
import me.wobbychip.smptweaks.custom.globaltrading.Events;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class PreventDropCentering extends CustomTweak {
	public PreventDropCentering() {
		super("PreventDropCentering");

		if (this.isEnabled()) {
			if (PaperUtils.isPaper) {
				Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
				this.printEnabled();
			} else {
				Utils.sendMessage("[PreventDropCentering] Ment only for PaperMC.");
			}
		} else {
			this.printDisabled();
		}
	}
}
