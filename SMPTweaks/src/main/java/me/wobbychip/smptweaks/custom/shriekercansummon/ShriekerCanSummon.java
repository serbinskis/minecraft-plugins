package me.wobbychip.smptweaks.custom.shriekercansummon;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class ShriekerCanSummon extends CustomTweak {
	public static String TAG_IS_PLAYER_PLACED = "isPlayerPlaced";
	public static int WARDEN_SPAWN_DISATNCE = 10;

	public ShriekerCanSummon() {
		super(ShriekerCanSummon.class, false, false);
		this.setDescription("Allow player to fuel up shrieker with 2 soul sand and summon warden once.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
