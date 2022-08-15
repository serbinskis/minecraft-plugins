package me.wobbychip.smptweaks.custom.custompotions;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.custompotions.commands.Commands;
import me.wobbychip.smptweaks.custom.custompotions.commands.TabCompletion;
import me.wobbychip.smptweaks.custom.custompotions.events.BowEvents;
import me.wobbychip.smptweaks.custom.custompotions.events.InventoryEvents;
import me.wobbychip.smptweaks.custom.custompotions.events.PotionEvents;
import me.wobbychip.smptweaks.custom.custompotions.events.ProjectileEvents;
import me.wobbychip.smptweaks.custom.custompotions.events.VillagerEvents;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.custom.custompotions.potions.PotionManager;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class CustomPotions extends CustomTweak {
	public static String customTag = "CustomPotion";
	public static int tradingPotionChance = 5;
	public static int tradingArrowChance = 5;
	public static CustomPotions tweak;
	public static Config config;
	public static PotionManager manager;

	public CustomPotions() {
		super(CustomPotions.class.getSimpleName(), false, false);
	}

	public void onEnable() {
		loadConfig();
		CustomPotions.tweak = this;
		manager = new PotionManager();
		boolean allowVillagerTrading = CustomPotions.config.getConfig().getConfigurationSection("config").getBoolean("allowVillagerTrading");

		for (CustomPotion potion : manager.getPotions(Main.classLoader, "me.wobbychip.smptweaks.custom.custompotions.custom")) {
			if (!allowVillagerTrading) { potion.setAllowVillagerTrades(allowVillagerTrading); }
			manager.registerPotion(potion);
		}

		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new BowEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new ProjectileEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new VillagerEvents(), Main.plugin);

		Main.plugin.getCommand("cpotions").setExecutor(new Commands());
		Main.plugin.getCommand("cpotions").setTabCompleter(new TabCompletion());
		CustomPotions.tweak.printMessage("Potions: " + manager.getPotionsString(), true);
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(CustomPotions.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		CustomPotions.config = new Config(configPath, "/tweaks/CustomPotions/config.yml");

		CustomPotions.tradingPotionChance = CustomPotions.config.getConfig().getInt("tradingPotionChance");
		CustomPotions.tradingArrowChance = CustomPotions.config.getConfig().getInt("tradingArrowChance");
	}
}
