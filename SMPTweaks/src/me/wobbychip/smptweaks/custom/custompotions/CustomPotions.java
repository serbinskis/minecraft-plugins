package me.wobbychip.smptweaks.custom.custompotions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import com.google.common.reflect.ClassPath;

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
import me.wobbychip.smptweaks.utils.Utils;

public class CustomPotions extends CustomTweak {
	public static Config config;
	public static PotionManager manager;

	public CustomPotions() {
		super("CustomPotions");

		if (this.isEnabled()) {
			loadConfig();
			onEnable();
		} else {
			this.printDisabled();
		}
	}

	public void onEnable() {
		manager = new PotionManager();
		boolean allowVillagerTrading = CustomPotions.config.getConfig().getConfigurationSection("config").getBoolean("allowVillagerTrading");

		for (CustomPotion potion : getPotions("me.wobbychip.smptweaks.custom.custompotions.custom")) {
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

		Utils.sendMessage("&9[CustomPotions] CustomPotions has loaded!");
		Utils.sendMessage("&9[CustomPotions] Potions: " + manager.getPotionsString());
	}

	public List<CustomPotion> getPotions(String pacakgeName) {
		List<CustomPotion> potions = new ArrayList<>();

		try {
			ClassPath classPath = ClassPath.from(Main.classLoader);

			for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses(pacakgeName)) {
				Class<?> clazz = Class.forName(classInfo.getName(), true, Main.classLoader);
				potions.add((CustomPotion) clazz.getConstructor().newInstance());
			}
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		return potions;
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(CustomPotions.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		CustomPotions.config = new Config(configPath, "/tweaks/CustomPotions/config.yml");
	}
}
