package me.wobbychip.custompotions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.ClassPath;

import me.wobbychip.custompotions.commands.Commands;
import me.wobbychip.custompotions.commands.TabCompletion;
import me.wobbychip.custompotions.events.BowEvents;
import me.wobbychip.custompotions.events.ProjectileEvents;
import me.wobbychip.custompotions.events.VillagerEvents;
import me.wobbychip.custompotions.events.InventoryEvents;
import me.wobbychip.custompotions.events.PotionEvents;
import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.potions.PotionManager;
import me.wobbychip.custompotions.utils.Utils;

public class Main extends JavaPlugin {
	public static Main plugin;
	public static PotionManager manager;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();
		manager = new PotionManager();

		for (CustomPotion potion : getPotions("me.wobbychip.custompotions.custom")) {
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
			ClassPath classPath = ClassPath.from(Main.plugin.getClassLoader());

			for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses(pacakgeName)) {
				Class<?> clazz = Class.forName(classInfo.getName(), true, Main.plugin.getClassLoader());
				potions.add((CustomPotion) clazz.getConstructor().newInstance());
			}
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		return potions;
	}
}