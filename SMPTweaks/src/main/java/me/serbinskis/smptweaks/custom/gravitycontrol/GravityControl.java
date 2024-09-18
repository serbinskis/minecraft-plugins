package me.serbinskis.smptweaks.custom.gravitycontrol;

import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class GravityControl extends CustomTweak {
	public static CustomTweak tweak;

	public GravityControl() {
		super(GravityControl.class, true, false);
		this.setGameRule("doGravityControl", true, false);
		this.setDescription("Enable falling block duplication glitch with end portal on PaperMC servers.");
		GravityControl.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
