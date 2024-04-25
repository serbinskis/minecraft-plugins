package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.customworld.commands.Commands;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

public class CustomWorlds extends CustomTweak {
	public static CustomTweak tweak;
	public static String TAG_CUSTOM_WORLD = "SMPTWEAKS_CUSTOM_WORLD";
	public static String TAG_BIOME_NAME = "SMPTWEAKS_CUSTOM_BIOME";
	public static String TAG_BIOME_NAMESPACE = "SMPTWEAKS";
	public Commands commands;

	public CustomWorlds() {
		super(CustomWorlds.class, false, true);
		this.setCommand(new Commands(this, "cworld"));
		this.setDescription("Custom world and biome settings. (VERY UNSTABLE)");
		this.setStartup(true);
		CustomWorlds.tweak = this;
	}

	public void onEnable() {
		new ProtocolEvents(Main.plugin);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
