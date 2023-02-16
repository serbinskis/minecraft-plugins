package me.wobbychip.smptweaks.custom.noendportal;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class NoEndPortal extends CustomTweak {
	public NoEndPortal() {
		super(NoEndPortal.class.getSimpleName(), false, false);
		this.setDescription("Disable end portal with custom gamerule (doEndPortal, default: true).");
	}

	public void onEnable() {
		Main.gameRules.addGameRule("doEndPortal", true);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
