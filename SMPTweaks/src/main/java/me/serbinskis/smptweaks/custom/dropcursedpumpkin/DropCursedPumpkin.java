package me.serbinskis.smptweaks.custom.dropcursedpumpkin;

import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class DropCursedPumpkin extends CustomTweak {
	public static CustomTweak tweak;

	public DropCursedPumpkin() {
		super(DropCursedPumpkin.class, false, false);
		this.setDescription("Drop cursed pumpkin on death when keep inventory is enabled.");
		this.setGameRule("doDropCursedPumpkin", true, false);
		DropCursedPumpkin.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
