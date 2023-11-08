package me.wobbychip.smptweaks.custom.custompotions;

import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.custompotions.commands.Commands;
import me.wobbychip.smptweaks.custom.custompotions.events.BowEvents;
import me.wobbychip.smptweaks.custom.custompotions.events.InventoryEvents;
import me.wobbychip.smptweaks.custom.custompotions.events.PotionEvents;
import me.wobbychip.smptweaks.custom.custompotions.events.ProjectileEvents;
import me.wobbychip.smptweaks.custom.custompotions.events.VillagerEvents;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.custom.custompotions.potions.PotionManager;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class CustomPotions extends CustomTweak {
	public static CustomTweak tweak;
	public static String customTag = "CustomPotion";
	public static int tradingPotionChance = 5;
	public static int tradingArrowChance = 5;
	public static Config config;
	public static PotionManager manager;
	public Commands comands;

	public CustomPotions() {
		super(CustomPotions.class, false, false);
		this.comands = new Commands(this, "cpotions");
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doCustomPotion", true, false);
		this.setDescription("Adds to the server different new potions and new brewing recipes. " +
							"To get more info about potions use command /smptweaks execute info.");
		
	}

	public void onEnable() {
		this.onReload();
		this.setCommand(this.comands);
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

		CustomPotions.tweak.printMessage("Potions: " + manager.getPotionsString(), true);
	}

	public void onReload() {
		CustomPotions.config = this.getConfig(0);
		CustomPotions.tradingPotionChance = CustomPotions.config.getConfig().getConfigurationSection("config").getInt("tradingPotionChance");
		CustomPotions.tradingArrowChance = CustomPotions.config.getConfig().getConfigurationSection("config").getInt("tradingArrowChance");
	}
}
