package me.wobbychip.smptweaks.custom.silktouchspawners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class SilkTouchSpawners extends CustomTweak {
	public static List<Material> correctTools = new ArrayList<>();
	public static List<String> silks = Arrays.asList("silk_touch");

	public SilkTouchSpawners() {
		super(SilkTouchSpawners.class, false, false);
		this.setConfigs(List.of("config.yml"));
		this.setReloadable(true);
		this.setDescription("Allows getting spawners with a silk touch.");
	}

	public void onEnable() {
		this.onReload();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		List<String> stringList = this.getConfig(0).getConfig().getStringList("correctTools");
		SilkTouchSpawners.correctTools = stringList.stream().map(Material::valueOf).collect(Collectors.toList());
	}
}
