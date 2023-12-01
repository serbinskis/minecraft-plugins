package me.wobbychip.smptweaks.custom.antiendermangrief;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class AntiEndermanGrief extends CustomTweak {
	public static CustomTweak tweak;

	public AntiEndermanGrief() {
		super(AntiEndermanGrief.class, false, false);
		this.setGameRule("doEndermanGrief", false, false);
		this.setDescription("Prevent enderman from picking up blocks with gamerule.");
		AntiEndermanGrief.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
