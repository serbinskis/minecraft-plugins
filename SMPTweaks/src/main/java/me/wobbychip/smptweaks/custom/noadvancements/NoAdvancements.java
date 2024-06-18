package me.wobbychip.smptweaks.custom.noadvancements;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

public class NoAdvancements extends CustomTweak {
	public static CustomTweak tweak;

	public NoAdvancements() {
		super(NoAdvancements.class, false, false);
		this.setGameRule("doAdvancements", true, false);
		this.setDescription("Disable advancements with custom gamerule.");
		NoAdvancements.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
