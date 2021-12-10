package me.wobbychip.recallpotion;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.recallpotion.events.BowEvents;
import me.wobbychip.recallpotion.events.DispenserEvents;
import me.wobbychip.recallpotion.events.InventoryEvents;
import me.wobbychip.recallpotion.events.PotionEvents;
import me.wobbychip.recallpotion.potions.PotionManager;
import me.wobbychip.recallpotion.utils.Utils;

public class Main extends JavaPlugin {
	public static Plugin plugin;
	public static PotionManager manager;

	@Override
	public void onEnable() {
		manager = new PotionManager();
		manager.registerPotion(new RecallPotion());

		Main.plugin = this;
		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new BowEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new DispenserEvents(), Main.plugin);
		Utils.sendMessage("&9[RecallPotion] RecallPotion has loaded!");
	}
}