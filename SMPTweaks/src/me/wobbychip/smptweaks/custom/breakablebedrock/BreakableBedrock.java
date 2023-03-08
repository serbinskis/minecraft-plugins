package me.wobbychip.smptweaks.custom.breakablebedrock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

//There is an issue when breaking and pressing ESC the progress will continue
//This is client side issue which is impossible to fix until Mojang themselves do it

public class BreakableBedrock extends CustomTweak {
	public static double destroyTime = -1.0F;
	public static boolean shouldDrop = false;
	public static boolean preventPacket = true;
	public static boolean enableTimer = false;
	public static List<Material> correctTools = new ArrayList<>();

	public BreakableBedrock() {
		super(BreakableBedrock.class, false, false);
		this.setConfigs(List.of("config.yml"));
		this.setReloadable(true);
		this.setDescription("Allows you to destroy bedrock and collect it.");
	}

	public void onEnable() {
		this.onReload();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		BreakableBedrock.destroyTime = this.getConfig(0).getConfig().getDouble("destroyTime");
		BreakableBedrock.shouldDrop = this.getConfig(0).getConfig().getBoolean("shouldDrop");
		BreakableBedrock.enableTimer = this.getConfig(0).getConfig().getBoolean("enableTimer");

		List<String> stringList = this.getConfig(0).getConfig().getStringList("correctTools");
		BreakableBedrock.correctTools = stringList.stream().map(Material::valueOf).collect(Collectors.toList());
	}
}
