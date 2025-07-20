package me.serbinskis.smptweaks.custom.preventdropcentering;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;

public class PreventDropCentering extends CustomTweak {
	public PreventDropCentering() {
		super(PreventDropCentering.class, true, false);
		this.setDescription("Prevent item from centering in the middle when block breaks on PaperMC servers.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
