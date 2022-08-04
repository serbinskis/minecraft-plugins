package me.wobbychip.smptweaks.custom.globaltrading;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class GlobalTrading extends CustomTweak {
	public GlobalTrading() {
		super(GlobalTrading.class.getSimpleName(), true);
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
