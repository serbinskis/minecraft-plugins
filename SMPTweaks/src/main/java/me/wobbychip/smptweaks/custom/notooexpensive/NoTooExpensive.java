package me.wobbychip.smptweaks.custom.notooexpensive;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class NoTooExpensive extends CustomTweak {
	public static CustomTweak tweak;
	public static int MAXIMUM_REPAIR_COST = 40;

	public NoTooExpensive() {
		super(NoTooExpensive.class, false, false);
		NoTooExpensive.tweak = this;
		this.setGameRule("doNoTooExpensive", true, false);
		this.setDescription("Removes \"Too expensive\" from anvils and allows you to spend any level if you have enough.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
