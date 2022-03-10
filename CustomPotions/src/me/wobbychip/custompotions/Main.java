package me.wobbychip.custompotions;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.custompotions.commands.Commands;
import me.wobbychip.custompotions.commands.TabCompletion;
import me.wobbychip.custompotions.custom.ExplosionPotion;
import me.wobbychip.custompotions.custom.FirePotion;
import me.wobbychip.custompotions.custom.LaunchPotion;
import me.wobbychip.custompotions.custom.LightingPotion;
import me.wobbychip.custompotions.custom.RecallPotion;
import me.wobbychip.custompotions.custom.UnbindingPotion;
import me.wobbychip.custompotions.custom.VoidPotion;
import me.wobbychip.custompotions.events.BowEvents;
import me.wobbychip.custompotions.events.ProjectileEvents;
import me.wobbychip.custompotions.events.InventoryEvents;
import me.wobbychip.custompotions.events.PotionEvents;
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
		manager.registerPotion(new ExplosionPotion());
		manager.registerPotion(new FirePotion());
		manager.registerPotion(new LaunchPotion());
		manager.registerPotion(new LightingPotion());
		manager.registerPotion(new RecallPotion());
		manager.registerPotion(new UnbindingPotion());
		manager.registerPotion(new VoidPotion());

		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new BowEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new ProjectileEvents(), Main.plugin);

		Main.plugin.getCommand("cpotions").setExecutor(new Commands());
		Main.plugin.getCommand("cpotions").setTabCompleter(new TabCompletion());

		Utils.sendMessage("&9[CustomPotions] CustomPotions has loaded!");
		Utils.sendMessage("&9[CustomPotions] Potions: " + manager.getPotions());
	}
}