package me.serbinskis.smptweaks.custom.fastcuring;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import java.util.List;

public class FastCuring extends CustomTweak {
	public static CustomTweak tweak;
	public static int intervalTicks;

	public FastCuring() {
		super(FastCuring.class, false, false);
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doFastCuring", true, false);
		this.setReloadable(true);
		this.setDescription("Makes curing villagers much faster.");
		FastCuring.tweak = this;
	}

	public void onEnable() {
		this.onReload();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		FastCuring.intervalTicks = this.getConfig(0).getConfig().getInt("intervalTicks");
	}
}
