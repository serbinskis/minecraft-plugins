package me.serbinskis.smptweaks.custom.globaltrading;

import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class GlobalTrading extends CustomTweak {
	public static CustomTweak tweak;

	public GlobalTrading() {
		super(GlobalTrading.class, false, false);
		this.setGameRule("villager_global_trading", true, false);
		this.setDescription("Shares cured villager prices among all players.");
		GlobalTrading.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
