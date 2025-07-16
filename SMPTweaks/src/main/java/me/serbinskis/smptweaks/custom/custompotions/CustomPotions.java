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
		this.setGameRule("doCustomPotions", true, false);
		this.setDescription("Adds to the server different new potions and new brewing recipes. " +
							"To get more info about potions use command /smptweaks execute info.");
		CustomPotions.tweak = this;
	}

	public void onEnable() {
		this.onReload();
		boolean allowVillagerTrading = CustomPotions.config.getConfig().getConfigurationSection("config").getBoolean("allowVillagerTrading");

		for (CustomPotion potion : ReflectionUtils.getInstances(Main.plugin.getPluginClassLoader(), POTIONS_PACKAGE, CustomPotion.class, true, false, true)) {
			if (!allowVillagerTrading) { potion.setAllowVillagerTrades(false); }
			PotionManager.registerPotion(potion);
		}

		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new ProjectileEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new VillagerEvents(), Main.plugin);
		CustomPotions.tweak.printMessage("Potions: " + PotionManager.getPotionsString(), true);
	}

	public void onReload() {
		CustomPotions.config = this.getConfig(0);
		CustomPotions.tradingPotionChance = CustomPotions.config.getConfig().getConfigurationSection("config").getInt("tradingPotionChance");
		CustomPotions.tradingArrowChance = CustomPotions.config.getConfig().getConfigurationSection("config").getInt("tradingArrowChance");
	}
}
