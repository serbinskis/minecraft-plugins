package me.wobbychip.smptweaks.custom.gravitycontrol;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class GravityControl extends CustomTweak {
	public static CustomTweak tweak;

	public GravityControl() {
		super(GravityControl.class.getSimpleName(), true, false);
		GravityControl.tweak = this;
		this.setGameRule("doGravityControl", true, false);
		this.setDescription("Enable falling block duplication glitch with end portal on PaperMC servers.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
