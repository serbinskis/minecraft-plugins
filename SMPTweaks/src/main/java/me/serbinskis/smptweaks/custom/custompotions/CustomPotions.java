package me.serbinskis.smptweaks.custom.custompotions;

import me.serbinskis.smptweaks.Config;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.custompotions.commands.Commands;
import me.serbinskis.smptweaks.custom.custompotions.events.*;
import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.custom.custompotions.potions.PotionManager;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;

import java.util.List;

public class CustomPotions extends CustomTweak {
	public static CustomTweak tweak;
	public final static String POTIONS_PACKAGE = "me.serbinskis.smptweaks.custom.custompotions.custom";
	public final static String TAG_CUSTOM_POTION = "CustomPotion";
	public static int tradingPotionChance = 5;
	public static int tradingArrowChance = 5;
	public static Config config;

	public CustomPotions() {
		super(CustomPotions.class, true, false);
		this.setCommand(new Commands(this, "cpotions"));
		this.setConfigs(List.of("config.yml"));
		this.setReloadable(true);
		this.setGameRule("custom_potions", true, false);
		this.setDescription("Adds to the server different new potions and new brewing recipes. " +
							"To get more info about potions use command /smptweaks execute info.");
		CustomPotions.tweak = this;
	}

	public void onEnable() {
		this.onReload();
		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.getPlugin());
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.getPlugin());
		Bukkit.getPluginManager().registerEvents(new ProjectileEvents(), Main.getPlugin());
		Bukkit.getPluginManager().registerEvents(new VillagerEvents(), Main.getPlugin());
	}

	public void onReload() {
		CustomPotions.config = this.getConfig(0);

		PotionManager.unregisterAll();
		boolean allowVillagerTrading = CustomPotions.config.getConfig().getConfigurationSection("config").getBoolean("allowVillagerTrading");
		List<CustomPotion> instances = ReflectionUtils.getInstances(Main.getPluginClassLoader(), POTIONS_PACKAGE, CustomPotion.class, true, false, true);
		instances.forEach(customPotion -> customPotion.setAllowVillagerTrades(allowVillagerTrading));
		instances.forEach(PotionManager::registerPotion);
		CustomPotions.tweak.printMessage("Potions: " + PotionManager.getPotionsString(), true);

		CustomPotions.tradingPotionChance = CustomPotions.config.getConfig().getConfigurationSection("config").getInt("tradingPotionChance");
		CustomPotions.tradingArrowChance = CustomPotions.config.getConfig().getConfigurationSection("config").getInt("tradingArrowChance");
	}
}
