package me.wobbychip.smptweaks.custom.entitylimit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class EntityLimit extends CustomTweak {
	public static List<String> excludeReason = new ArrayList<>();
	public static int maximumDistance = 0;
	public static int limit = 0;
	public static Config config;

	public EntityLimit() {
		super(EntityLimit.class.getSimpleName(), false, false);
		this.setReloadable(true);
		this.setDescription("Limits entity spawn for each entity type.");
	}

	public void onEnable() {
		loadConfig();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		loadConfig();
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(EntityLimit.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";

		EntityLimit.config = new Config(configPath, "/tweaks/EntityLimit/config.yml");
		EntityLimit.excludeReason = EntityLimit.config.getConfig().getStringList("excludeReason");
		EntityLimit.maximumDistance = EntityLimit.config.getConfig().getInt("maximumDistance");
		EntityLimit.limit = EntityLimit.config.getConfig().getInt("limit");
	}
}
