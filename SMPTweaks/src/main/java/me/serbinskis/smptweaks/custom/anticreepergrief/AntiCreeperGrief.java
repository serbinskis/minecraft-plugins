package me.serbinskis.smptweaks.custom.anticreepergrief;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import me.serbinskis.smptweaks.Main;

public class AntiCreeperGrief extends CustomTweak {
	public static CustomTweak tweak;

	public AntiCreeperGrief() {
		super(AntiCreeperGrief.class, false, false);
		this.setGameRule("doCreeperGrief", false, false);
		this.setDescription("Prevent creepers from exploding blocks with gamerule.");
		AntiCreeperGrief.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
