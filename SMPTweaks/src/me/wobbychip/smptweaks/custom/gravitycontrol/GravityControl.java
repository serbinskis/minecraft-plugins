package me.wobbychip.smptweaks.custom.gravitycontrol;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class GravityControl extends CustomTweak {
	public GravityControl() {
		super(GravityControl.class.getSimpleName(), true, false);
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
