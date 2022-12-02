package me.wobbychip.smptweaks.custom.noadvancements;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class NoAdvancements extends CustomTweak {
	public static GameRules gameRules = new GameRules(Main.plugin);

	public NoAdvancements() {
		super(NoAdvancements.class.getSimpleName(), false, true);
		this.setDescription("Disable advancements with custom gamerule.");
	}

	public void onEnable() {
		gameRules.addGameRule("doAdvancements", true);
		new ProtocolEvents(Main.plugin);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
