package me.serbinskis.smptweaks.custom.dropcursedpumpkin;

import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class DropCursedPumpkin extends CustomTweak {
	public DropCursedPumpkin() {
		super(DropCursedPumpkin.class, false, false);
		this.setDescription("Drop cursed pumpkin on death when keep inventory is enabled.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
