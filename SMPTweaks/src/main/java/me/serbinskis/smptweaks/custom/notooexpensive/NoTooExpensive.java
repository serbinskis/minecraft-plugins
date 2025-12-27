package me.serbinskis.smptweaks.custom.notooexpensive;

import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class NoTooExpensive extends CustomTweak {
	public static CustomTweak tweak;
	public static int MAXIMUM_REPAIR_COST = 40;

	public NoTooExpensive() {
		super(NoTooExpensive.class, false, false);
		this.setGameRule("disable_anvil_too_expensive", true, false);
		this.setDescription("Removes \"Too expensive\" from anvils and allows you to spend any level if you have enough.");
		NoTooExpensive.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
