package me.serbinskis.smptweaks.custom.antiendermangrief;

import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class AntiEndermanGrief extends CustomTweak {
	public static CustomTweak tweak;

	public AntiEndermanGrief() {
		super(AntiEndermanGrief.class, false, false);
		this.setGameRule("enderman_grief", true, false);
		this.setDescription("Prevent enderman from picking up blocks with gamerule.");
		AntiEndermanGrief.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
