package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.customworld.commands.Commands;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

public class CustomWorld extends CustomTweak {
	public static CustomTweak tweak;
	public static String TAG_CUSTOM_WORLD = "SMPTWEAKS_CUSTOM_WORLD";
	public enum Type { END, VOID, NONE }
	public Commands commands;

	public CustomWorld() {
		super(CustomWorld.class, false, true);
		this.setCommand(new Commands(this, "cworld"));
		this.setDescription("Custom world setting for my server. (VERY BROKEN)");
		this.setStartup(true);
		CustomWorld.tweak = this;
	}

	public void onEnable() {
		new ProtocolEvents(Main.plugin);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public static Type getCustomType(String string) {
		try {
			return CustomWorld.Type.valueOf(string.toUpperCase());
		} catch (Exception e) { return null; }
	}
}
