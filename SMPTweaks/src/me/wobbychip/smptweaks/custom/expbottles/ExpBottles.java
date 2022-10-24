package me.wobbychip.smptweaks.custom.expbottles;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class ExpBottles extends CustomTweak {
	public ExpBottles() {
		super(ExpBottles.class.getSimpleName(), false, false);
		this.setDescription("Allow filling empty bottles at the enchantment table. " +
							"Right click on the enchantment table with an empty bottle while crouching.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
