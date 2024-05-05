package me.wobbychip.smptweaks.custom.globaltrading;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class GlobalTrading extends CustomTweak {
	public static CustomTweak tweak;

	public GlobalTrading() {
		super(GlobalTrading.class, false, false);
		this.setGameRule("doGlobalTrading", true, false);
		this.setDescription("Shares cured villager prices among all players.");
		GlobalTrading.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
