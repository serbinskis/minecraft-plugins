package me.wobbychip.mendfinity;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Utilities {
	public static void DebugInfo(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	//Mendfinity bow item
	public static ItemStack mendifnityBow() {
		ItemStack mendifnity = new ItemStack(Material.BOW);
		mendifnity.addEnchantment(Enchantment.MENDING, 1);
		mendifnity.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		return mendifnity;
	}

	//Create bow with specific enchantment
	public static ItemStack createBow(Enchantment enchantment) {
		ItemStack bow = new ItemStack(Material.BOW);
		bow.addEnchantment(enchantment, 1);

		ItemMeta bowMeta = bow.getItemMeta();
		bowMeta.setUnbreakable(true);
		bowMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		bow.setItemMeta(bowMeta);

		return bow;
	}

	//Check if bow is with specific parameters
	@SuppressWarnings("deprecation")
	public static boolean checkEnchantedBow(ItemStack item, Enchantment enchantment, int level) {
		if (item.getType() != Material.BOW) {
			return false;
		}

		if (item.getDurability() > 0) {
			return false;
		}

		if (item.getEnchantmentLevel(enchantment) != level) {
			return false;
		}

		if (item.getEnchantments().size() > 1) {
			return false;
		}

		return true;
	}

	//Check crafting recipe
	public static void checkCrafting(Inventory inventory) {
		ItemStack[] inv = inventory.getStorageContents();

		if (inv[5].getType() != Material.NETHER_STAR) { return; }
		if (inv[1].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[2].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[3].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[7].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[8].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[9].getType() != Material.EXPERIENCE_BOTTLE) { return; }

		if ((checkEnchantedBow(inv[4], Enchantment.MENDING, 1) && checkEnchantedBow(inv[6], Enchantment.ARROW_INFINITE, 1)) ||
			(checkEnchantedBow(inv[6], Enchantment.MENDING, 1) && checkEnchantedBow(inv[4], Enchantment.ARROW_INFINITE, 1))) {

			inventory.setItem(0, mendifnityBow());
		} else {
			inventory.setItem(0, new ItemStack(Material.AIR));
		}

		((Player) inventory.getHolder()).updateInventory();
	}

	//Remove ingredients from crafting and update inventory
	public static void clearCrafting(InventoryClickEvent event) {
    	for (int i = 1; i <= 9; i++) {
    		ItemStack slot = event.getInventory().getStorageContents()[i];
    		slot.setAmount(slot.getAmount()-1);
    	}

    	event.getInventory().setItem(0, new ItemStack(Material.AIR));
    	Player player = (Player) event.getWhoClicked();
    	player.updateInventory();
	}

	//Drop item from player position
	public static void dropItem(Player player, ItemStack item) {
		Location location = player.getLocation();
		location.setY(location.getY()+1.3);

		Vector vector = player.getLocation().getDirection();
		vector.multiply(0.32);

		Item itemDropped = player.getWorld().dropItem(location, item);
		itemDropped.setVelocity(vector);
		itemDropped.setPickupDelay(40);
	}

	//Find first empty slot
	public static int findEmptySlot(PlayerInventory inventory) {
		ItemStack[] inv = inventory.getStorageContents();

		for (int i = 8; i >= 0; i--) {
			if (inv[i] == null) { return i; }
		}

		for (int i = 35; i >= 9; i--) {
			if (inv[i] == null) { return i; }
		}

		return -1;
	}
}