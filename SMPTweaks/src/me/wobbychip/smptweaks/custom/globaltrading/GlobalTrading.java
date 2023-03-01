package me.wobbychip.smptweaks.custom.globaltrading;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class GlobalTrading extends CustomTweak {
	public static CustomTweak tweak;

	public GlobalTrading() {
		super(GlobalTrading.class.getSimpleName(), true, false);
		GlobalTrading.tweak = this;
		this.setGameRule("doGlobalTrading", true);
		this.setDescription("Shares cured villager price among all players.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
