package me.wobbychip.smptweaks.custom.dropcursedpumpkin;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class DropCursedPumpkin extends CustomTweak {
	public DropCursedPumpkin() {
		super(DropCursedPumpkin.class.getSimpleName(), false, false);
		this.setDescription("Drop cursed pumpkin on death when keep inventory is enabled.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
