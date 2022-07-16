package me.wobbychip.smptweaks.custom.fastcuring;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class FastCuring extends CustomTweak {
	public static int intervalTicks;
	public static Config config;

	public FastCuring() {
		super("FastCuring");

		if (this.isEnabled()) {
			loadConfig();
			Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
			this.printEnabled();
		} else {
			this.printDisabled();
		}
	}

	public static void loadConfig() {
        List<String> list = Arrays.asList(FastCuring.class.getCanonicalName().split("\\."));
        String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		FastCuring.config = new Config(configPath, "/tweaks/FastCuring/config.yml");
		FastCuring.intervalTicks = FastCuring.config.getConfig().getInt("intervalTicks");
	}
}