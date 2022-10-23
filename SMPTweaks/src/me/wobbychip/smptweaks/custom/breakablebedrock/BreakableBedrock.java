package me.wobbychip.smptweaks.custom.breakablebedrock;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class BreakableBedrock extends CustomTweak {
	public static Config config;
	public static double destroyTime = -1.0F;
	public static boolean shouldDrop = false;
	public static boolean preventPacket = true;

	public BreakableBedrock() {
		super(BreakableBedrock.class.getSimpleName(), false, true);
		this.setDescription("Allows you to destroy bedrock and collect it.");
		this.setReloadable(true);
		this.onReload();
	}

	public void onEnable() {
		//new ProtocolEvents(Main.plugin);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		loadConfig();
		BedrockBreaker.cache.clear();
		BedrockBreaker.cache.put(Material.BEDROCK, (float) destroyTime);
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(BreakableBedrock.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		BreakableBedrock.config = new Config(configPath, "/tweaks/BreakableBedrock/config.yml");

		BreakableBedrock.destroyTime = BreakableBedrock.config.getConfig().getDouble("destroyTime");
		BreakableBedrock.shouldDrop = BreakableBedrock.config.getConfig().getBoolean("shouldDrop");
	}
}
