package me.wobbychip.smptweaks.custom.noendportal;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class NoEndPortal extends CustomTweak {
	public static CustomTweak tweak;

	public NoEndPortal() {
		super(NoEndPortal.class, false, false);
		this.setGameRule("doEndPortal", true, false);
		this.setDescription("Disable end portal with custom gamerule.");
		NoEndPortal.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
