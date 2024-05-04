package me.wobbychip.smptweaks.custom.noadvancements;

import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;

public class NoAdvancements extends CustomTweak {
	public static CustomTweak tweak;

	public NoAdvancements() {
		super(NoAdvancements.class, false, false);
		this.setGameRule("doAdvancements", true, false);
		this.setDescription("Disable advancements with custom gamerule.");
		NoAdvancements.tweak = this;
	}

	public void onEnable() {
		Utils.sendMessage("[NoAdvancements]: Not implemented");
		//Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
