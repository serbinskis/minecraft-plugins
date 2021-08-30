package me.wobbychip.recallpotion;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

import me.wobbychip.recallpotion.events.BlockEvents;
import me.wobbychip.recallpotion.events.InventoryEvents;
import me.wobbychip.recallpotion.events.PotionEvents;

public class Main extends JavaPlugin implements Listener {
	public static Plugin plugin;
	public static HashMap<Location, BrewManager> brews = new HashMap<Location, BrewManager>();
	public static int brewTime = 200; //Default 400
	public static PotionType potionBase = PotionType.MUNDANE;
	public static Material potionIngredient = Material.CHORUS_FRUIT;
	public static ItemStack potionItem = null;
	public static ItemStack splashPotionItem = null;
	public static ItemStack lingeringPotionItem = null;

	@Override
	public void onEnable() {
		potionItem = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
		potionMeta.setColor(Color.fromRGB(23, 193, 224));
		potionMeta.setDisplayName("§rPotion of Recalling");
		potionMeta.setLore(Arrays.asList("§9Teleport to Spawnpoint"));
		potionMeta.setLocalizedName("recall_potion");
		potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		potionItem.setItemMeta(potionMeta);

		splashPotionItem = new ItemStack(Material.SPLASH_POTION);
		PotionMeta splashPotionMeta = (PotionMeta) splashPotionItem.getItemMeta();
		splashPotionMeta.setColor(Color.fromRGB(23, 193, 224));
		splashPotionMeta.setDisplayName("§rSplash Potion of Recalling");
		splashPotionMeta.setLore(Arrays.asList("§9Teleport to Spawnpoint"));
		splashPotionMeta.setLocalizedName("recall_splash_potion");
		splashPotionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		splashPotionItem.setItemMeta(splashPotionMeta);
	
		lingeringPotionItem = new ItemStack(Material.LINGERING_POTION);
		PotionMeta lingeringPotionMeta = (PotionMeta) lingeringPotionItem.getItemMeta();
		lingeringPotionMeta.setColor(Color.fromRGB(23, 193, 224));
		lingeringPotionMeta.setDisplayName("§rLingering Potion of Recalling");
		lingeringPotionMeta.setLore(Arrays.asList("§9Teleport to Spawnpoint"));
		lingeringPotionMeta.setLocalizedName("recall_lingering_potion");
		lingeringPotionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		lingeringPotionItem.setItemMeta(lingeringPotionMeta);

		Main.plugin = this;
		Bukkit.getPluginManager().registerEvents(new PotionEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new BlockEvents(), Main.plugin);
		Utilities.sendMessage("&9[RecallPotion] RecallPotion has loaded!");
	}
}