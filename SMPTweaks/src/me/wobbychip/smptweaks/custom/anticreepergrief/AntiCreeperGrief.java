package me.wobbychip.smptweaks.custom.anticreepergrief;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class AntiCreeperGrief extends CustomTweak {
	public AntiCreeperGrief() {
		super(AntiCreeperGrief.class.getSimpleName(), false, false);
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
