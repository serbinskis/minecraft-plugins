package me.wobbychip.smptweaks.custom.fastcuring;

import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class FastCuring extends CustomTweak {
	public static CustomTweak tweak;
	public static int intervalTicks;

	public FastCuring() {
		super(FastCuring.class, false, false);
		FastCuring.tweak = this;
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doFastCuring", true, false);
		this.setReloadable(true);
		this.setDescription("Makes curing villagers much faster.");
	}

	public void onEnable() {
		this.onReload();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		FastCuring.intervalTicks = this.getConfig(0).getConfig().getInt("intervalTicks");
	}
}
