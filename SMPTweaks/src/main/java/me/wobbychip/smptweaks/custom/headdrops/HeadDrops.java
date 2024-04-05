package me.wobbychip.smptweaks.custom.headdrops;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class HeadDrops extends CustomTweak {
	public HeadDrops() {
		super(HeadDrops.class, false, false);
		this.setDescription("Drop player head on death if killed by player.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
