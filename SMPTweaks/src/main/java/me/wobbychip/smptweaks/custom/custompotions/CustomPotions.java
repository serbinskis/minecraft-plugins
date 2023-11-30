package me.wobbychip.smptweaks.custom.custompotions;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.custompotions.commands.Commands;
import me.wobbychip.smptweaks.custom.custompotions.events.*;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.custom.custompotions.potions.PotionManager;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import java.util.List;

public class CustomPotions extends CustomTweak {
	public static CustomTweak tweak;
	public final static String TAG_CUSTOM_POTION = "CustomPotion";
	public static int tradingPotionChance = 5;
	public static int tradingArrowChance = 5;
	public static Config config;
	public static PotionManager manager;

	public CustomPotions() {
		super(CustomPotions.class, false, false);
		this.setCommand(new Commands(this, "cpotions"));
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doCustomPotions", true, false);
		this.setDescription("Adds to the server different new potions and new brewing recipes. " +
							"To get more info about potions use command /smptweaks execute info.");
		CustomPotions.tweak = this;
	}

	public void onEnable() {
		this.onReload();
		manager = new PotionManager();
		boolean allowVillagerTrading = CustomPotions.config.getConfig().getConfigurationSection("config").getBoolean("allowVillagerTrading");

		for (CustomPotion potion : manager.getPotions(Main.plugin.getPluginClassLoader(), "me.wobbychip.smptweaks.custom.custompotions.custom")) {
			if (!allowVillagerTrading) { potion.setAllowVillagerTrades(false); }
			manager.registerPotion(potion);
		}

		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new BowEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new ProjectileEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new VillagerEvents(), Main.plugin);

		CustomPotions.tweak.printMessage("Potions: " + manager.getPotionsString(), true);
	}

	public void onReload() {
		CustomPotions.config = this.getConfig(0);
		CustomPotions.tradingPotionChance = CustomPotions.config.getConfig().getConfigurationSection("config").getInt("tradingPotionChance");
		CustomPotions.tradingArrowChance = CustomPotions.config.getConfig().getConfigurationSection("config").getInt("tradingArrowChance");
	}
}
