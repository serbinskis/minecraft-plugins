package me.wobbychip.smptweaks.custom.autocraft;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class AutoCraft extends CustomTweak {
	public static CustomTweak tweak;
	public static int craftCooldown = 8;
	public static String redstoneMode = "indirect";
	public static boolean allowBlockRecipeModification = true;
	public static Crafters crafters;
	public static Config config;

	public AutoCraft() {
		super(AutoCraft.class.getSimpleName(), false, false);
		AutoCraft.tweak = this;
		this.setReloadable(true);
		this.setGameRule("doAutoCraft", true, false);
		this.setDescription("Put on a dispenser an item frame with a crafting table. " +
							"Put a recipe inside the dispenser. " +
							"Input any container behind the dispenser, output in front.");
	}

	public void onEnable() {
		loadConfig();
		crafters = new Crafters();

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				crafters.handleCrafters();
			}
		}, 0L, craftCooldown);

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		loadConfig();
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(AutoCraft.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		AutoCraft.config = new Config(configPath, "/tweaks/AutoCraft/config.yml");

		AutoCraft.craftCooldown = AutoCraft.config.getConfig().getInt("craftCooldown");
		AutoCraft.redstoneMode = AutoCraft.config.getConfig().getString("redstoneMode");
		AutoCraft.allowBlockRecipeModification = AutoCraft.config.getConfig().getBoolean("allowBlockRecipeModification");
	}
}
