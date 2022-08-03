package me.wobbychip.smptweaks.custom.gravitycontrol;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.PaperUtils;

public class GravityControl extends CustomTweak {
	public GravityControl() {
		super("GravityControl");

		if (this.isEnabled()) {
			if (PaperUtils.isPaper) {
				Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
				this.printEnabled();
			} else {
				this.printMessage("Ment only for PaperMC.", true);
			}
		} else {
			this.printDisabled();
		}
	}
}
