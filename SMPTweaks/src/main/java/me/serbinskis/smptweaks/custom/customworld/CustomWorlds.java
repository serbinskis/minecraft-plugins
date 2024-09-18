package me.serbinskis.smptweaks.custom.customworld;

import me.serbinskis.smptweaks.custom.customworld.commands.Commands;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.Main;
import org.bukkit.Bukkit;

public class CustomWorlds extends CustomTweak {
	public static CustomTweak tweak;
	public static String TAG_CUSTOM_WORLD = "SMPTWEAKS_CUSTOM_WORLD";
	public static String TAG_BIOME_NAME = "SMPTWEAKS_CUSTOM_BIOME";
	public static String TAG_BIOME_NAMESPACE = "SMPTWEAKS";
	public Commands commands;

	public CustomWorlds() {
		super(CustomWorlds.class, false, false);
		this.setCommand(new Commands(this, "cworld"));
		this.setDescription("Custom world and biome settings. (VERY UNSTABLE)");
		this.setStartup(true);
		CustomWorlds.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
