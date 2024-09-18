package me.serbinskis.smptweaks.custom.custombreaking;

import me.serbinskis.smptweaks.custom.custombreaking.breaking.CustomBreaker;
import me.serbinskis.smptweaks.custom.custombreaking.custom.CustomBedrock;
import me.serbinskis.smptweaks.custom.custombreaking.custom.CustomSpawner;
import me.serbinskis.smptweaks.Config;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import java.util.List;

//There is an issue when breaking and pressing ESC the progress will continue
//This is client side issue which is impossible to fix until Mojang themselves do it

public class CustomBreaking extends CustomTweak {
	public static CustomTweak tweak;
	public static Config config;
	public static boolean enableTimer = Main.DEBUG_MODE;

	public CustomBreaking() {
		super(CustomBreaking.class, false, false);
		this.setConfigs(List.of("config.yml"));
		this.setDescription("Allows you to destroy block with custom speed and drop.");
		CustomBreaking.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
		CustomBreaking.config = getConfig(0);

		if (!config.getConfig().isConfigurationSection("blocks")) {
			config.getConfig().createSection("blocks");
			config.save();
		}

		CustomBreaker.addCustom(new CustomBedrock());
		CustomBreaker.addCustom(new CustomSpawner());
	}
}
