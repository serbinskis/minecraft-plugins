package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.customworld.commands.Commands;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

public class CustomWorld extends CustomTweak {
	public static CustomTweak tweak;
	public static String CUSTOM_WORLD_TAG = "SMPTWEAKS_CUSTOM_WORLD";
	public Commands comands;

	public CustomWorld() {
		super(CustomWorld.class, false, true);
		this.comands = new Commands(this, "cworld");
		this.setDescription("Custom world setting for my server.");
	}

	public void onEnable() {
		CustomWorld.tweak = this;
		this.setCommand(this.comands);

		new ProtocolEvents(Main.plugin);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
