package me.serbinskis.smptweaks.custom.headdrops;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;

public class HeadDrops extends CustomTweak {
	public static CustomTweak tweak;

	public HeadDrops() {
		super(HeadDrops.class, false, false);
		this.setDescription("Drop player head on death if killed by player.");
		this.setGameRule("doHeadDrops", false, false);
		HeadDrops.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
