package me.serbinskis.smptweaks.custom.headdrops;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;

public class HeadDrops extends CustomTweak {
	public HeadDrops() {
		super(HeadDrops.class, false, false);
		this.setDescription("Drop player head on death if killed by player.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
