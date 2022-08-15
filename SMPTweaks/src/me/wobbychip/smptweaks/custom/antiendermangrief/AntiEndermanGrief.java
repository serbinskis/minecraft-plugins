package me.wobbychip.smptweaks.custom.antiendermangrief;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class AntiEndermanGrief extends CustomTweak {
	public AntiEndermanGrief() {
		super(AntiEndermanGrief.class.getSimpleName(), false, false);
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
