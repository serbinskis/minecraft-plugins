package me.wobbychip.smptweaks.custom.shriekercansummon;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class ShriekerCanSummon extends CustomTweak {
	public static String isPlayerPlaced = "isPlayerPlaced";
	public static int WARDEN_SPAWN_DISATNCE = 10;

	public ShriekerCanSummon() {
		super(ShriekerCanSummon.class.getSimpleName(), false);
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
