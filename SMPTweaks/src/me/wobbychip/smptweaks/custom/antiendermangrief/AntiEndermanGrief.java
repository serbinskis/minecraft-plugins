package me.wobbychip.smptweaks.custom.antiendermangrief;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class AntiEndermanGrief extends CustomTweak {
	public static CustomTweak tweak;

	public AntiEndermanGrief() {
		super(AntiEndermanGrief.class.getSimpleName(), false, false);
		AntiEndermanGrief.tweak = this;
		this.setGameRule("doEndermanGrief", false, false);
		this.setDescription("Prevent enderman from picking up blocks with gamerule.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
