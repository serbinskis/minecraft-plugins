package me.wobbychip.smptweaks.custom.holograms;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class Holograms extends CustomTweak {
	public static String isHologram = "isHologram";

	public Holograms() {
		super(Holograms.class.getSimpleName(), false, true);
		this.setDescription("Admin tool to make holograms with armour stands. " +
				            "Permissions: smptweaks.holograms.*");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
