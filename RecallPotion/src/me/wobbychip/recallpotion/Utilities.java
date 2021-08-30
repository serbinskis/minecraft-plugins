package me.wobbychip.recallpotion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.util.Vector;

public class Utilities {
	public static void sendMessage(String arg0) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', arg0));
	}

	public static void respawnPlayer(Player player) {
		Location location = player.getBedSpawnLocation();

		if (location == null) {
			World world = Bukkit.getServer().getWorlds().get(0);
			location = world.getSpawnLocation();
			location.setX(location.getX()+0.5);
			location.setZ(location.getZ()+0.5);
			while ((location.getY() >= world.getMinHeight()) && (location.getBlock().getType() == Material.AIR)) { location.setY(location.getY()-1); }
			while ((location.getY() < world.getMaxHeight()) && (location.getBlock().getType() != Material.AIR)) { location.setY(location.getY()+1); }
		}

		location.setDirection(player.getLocation().getDirection());
		player.setVelocity(new Vector(0, 0, 0));
		player.setFallDistance(0);
		player.teleport(location);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
	}

	public static boolean isEmpty(ItemStack itemStack) {
		return ((itemStack == null) || (itemStack.getType() == Material.AIR));
	}

	public static boolean isEmpty(Block block) {
		return ((block == null) || (block.getType() == Material.AIR));
	}

	public static boolean isEquals(ItemStack item1, ItemStack item2) {
		ItemStack clone = item2.clone();
		clone.setAmount(item1.getAmount());
		return item1.equals(clone);
	}

	public static boolean isPotion(ItemStack itemStack) {
		return ((itemStack != null) && (itemStack.getType() == Material.POTION));
	}

	public static boolean isAnyPotion(ItemStack itemStack) {
		return ((itemStack != null) && ((itemStack.getType() == Material.POTION) || (itemStack.getType() == Material.SPLASH_POTION) || (itemStack.getType() == Material.LINGERING_POTION)));
	}

	public static void checkBrew(BrewingStand stand) {
		BrewerInventory inv = (BrewerInventory) stand.getInventory();
		Location location = stand.getLocation();

        //Check if ingredient still exists and if its valid
        if (Utilities.isEmpty(inv.getIngredient()) || (inv.getIngredient().getType() != Main.potionIngredient)) {
        	BrewManager brewManager = Main.brews.get(location);
        	if (brewManager != null) { brewManager.stop(); }
        	return;
        }

        //Check if stand was not destroyed
        if (Utilities.isEmpty(stand.getBlock())) {
        	BrewManager brewManager = Main.brews.get(location);
        	if (brewManager != null) { brewManager.stop(); }
        	return;
        }

        //Check if stand has valid potion
        int potionCounter = 0;
		for (int i = 0; i < 3; i++) {
			ItemStack item = inv.getItem(i);
			
			PotionMeta potionMeta = null;
			if (isPotion(item)) { potionMeta = (PotionMeta) item.getItemMeta(); }
			//If item not potion this causes error ^ - fixed

			PotionData potionData = null;
			if (potionMeta != null) { potionData = potionMeta.getBasePotionData(); }

			if ((potionData != null) && (potionData.getType() == Main.potionBase)) {
				potionCounter++;
			}
		}

		if (potionCounter == 0) {
        	BrewManager brewManager = Main.brews.get(location);
        	if (brewManager != null) { brewManager.stop(); }
			return;
		}

		//Create new brew if not existing
		BrewManager brewManager = Main.brews.get(location);
		if (brewManager == null) { new BrewManager(Main.plugin, stand, Main.brewTime); }
	}
}
