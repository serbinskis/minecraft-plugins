package me.wobbychip.smptweaks.custom.silktouchspawners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class SilkTouchSpawners extends CustomTweak {
	public static Config config;
	public static List<Material> correctTools = new ArrayList<>();
	public static List<String> silks = Arrays.asList("silk_touch");

	public SilkTouchSpawners() {
		super(SilkTouchSpawners.class.getSimpleName(), false, false);
		this.setReloadable(true);
		this.setDescription("Allow getting spawner with silk touch.");
	}

	public void onEnable() {
		loadConfig();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		loadConfig();
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(SilkTouchSpawners.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		SilkTouchSpawners.config = new Config(configPath, "/tweaks/SilkTouchSpawners/config.yml");

		List<String> stringList = SilkTouchSpawners.config.getConfig().getStringList("correctTools");
		SilkTouchSpawners.correctTools = stringList.stream().map(Material::valueOf).collect(Collectors.toList());
	}
}
