package me.serbinskis.smptweaks.custom.noendportal;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;

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
