package me.wobbychip.smptweaks.custom.noadvancements;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class NoAdvancements extends CustomTweak {
	public static CustomTweak tweak;

	public NoAdvancements() {
		super(NoAdvancements.class.getSimpleName(), false, true);
		NoAdvancements.tweak = this;
		this.setGameRule("doAdvancements", true, false);
		this.setDescription("Disable advancements with custom gamerule.");
	}

	public void onEnable() {
		new ProtocolEvents(Main.plugin);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
