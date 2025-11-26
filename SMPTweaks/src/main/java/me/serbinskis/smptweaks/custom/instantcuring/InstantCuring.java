package me.serbinskis.smptweaks.custom.instantcuring;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

public class InstantCuring extends CustomTweak {
	public static CustomTweak tweak;

	public InstantCuring() {
		super(InstantCuring.class, false, false);
		this.setGameRule("doInstantCuring", false, false);
		this.setDescription("Makes curing villagers instant.");
		InstantCuring.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
