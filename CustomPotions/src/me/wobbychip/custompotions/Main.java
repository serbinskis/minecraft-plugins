package me.wobbychip.custompotions;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.custompotions.custom.ExplosionPotion;
import me.wobbychip.custompotions.custom.RecallPotion;
import me.wobbychip.custompotions.custom.Unbinding;
import me.wobbychip.custompotions.custom.VoidPotion;
import me.wobbychip.custompotions.events.BowEvents;
import me.wobbychip.custompotions.events.DispenserEvents;
import me.wobbychip.custompotions.events.InventoryEvents;
import me.wobbychip.custompotions.events.PotionEvents;
import me.wobbychip.custompotions.potions.PotionManager;
import me.wobbychip.custompotions.utils.Utils;

public class Main extends JavaPlugin {
	public static Plugin plugin;
	public static PotionManager manager;

	@Override
	public void onEnable() {
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

		manager = new PotionManager();
		manager.registerPotion(new ExplosionPotion());
		manager.registerPotion(new RecallPotion());
		manager.registerPotion(new Unbinding());
		manager.registerPotion(new VoidPotion());

		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new BowEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new DispenserEvents(), Main.plugin);
		Utils.sendMessage("&9[CustomPotions] CustomPotions has loaded!");
		Utils.sendMessage("&9[CustomPotions] Potions: " + manager.getPotions());
	}
}