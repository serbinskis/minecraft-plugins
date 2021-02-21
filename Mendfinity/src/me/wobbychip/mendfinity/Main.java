package me.wobbychip.mendfinity;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {
	//Check if bow is with specific parameters
	@SuppressWarnings("deprecation")
	public boolean CheckEnchantedBow(ItemStack item, Enchantment enchantment, int level) {
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
	public void CheckCrafting(Inventory inventory, Player player) {
		ItemStack[] inv = inventory.getStorageContents();

		if (inv[5].getType() != Material.NETHER_STAR) { return; }
		if (inv[1].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[2].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[3].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[7].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[8].getType() != Material.EXPERIENCE_BOTTLE) { return; }
		if (inv[9].getType() != Material.EXPERIENCE_BOTTLE) { return; }

		if ((CheckEnchantedBow(inv[4], Enchantment.MENDING, 1) && CheckEnchantedBow(inv[6], Enchantment.ARROW_INFINITE, 1)) ||
			(CheckEnchantedBow(inv[6], Enchantment.MENDING, 1) && CheckEnchantedBow(inv[4], Enchantment.ARROW_INFINITE, 1))) {

			ItemStack mendifnity = new ItemStack(Material.BOW);
			mendifnity.addEnchantment(Enchantment.MENDING, 1);
			mendifnity.addEnchantment(Enchantment.ARROW_INFINITE, 1);
			inventory.setItem(0, mendifnity);
		} else {
			inventory.setItem(0, new ItemStack(Material.AIR));
		}

		player.updateInventory();
	}

	//Remove ingridients from crafting and update inventory
	public void ClearCrafting(InventoryClickEvent event) {
    	for (int i = 1; i <= 9; i++) {
    		ItemStack slot = event.getInventory().getStorageContents()[i];
    		slot.setAmount(slot.getAmount()-1);
    	}

    	event.getInventory().setItem(0, new ItemStack(Material.AIR));
    	Player player = (Player) event.getWhoClicked();
    	player.updateInventory();
	}

	//Drop item from player position
	public void DropItem(Player player, ItemStack item) {
		Location location = player.getLocation();
		location.setY(location.getY()+1.3);

		Vector vector = player.getLocation().getDirection();
		vector.multiply(0.32);

		Item itemDropped = player.getWorld().dropItem(location, item);
		itemDropped.setVelocity(vector);
		itemDropped.setPickupDelay(40);
	}

	//Find first empty slot
	public int FindEmptySlot(Player player) {
		ItemStack[] inv = player.getInventory().getStorageContents();

		for (int i = 8; i >= 0; i--) {
			if (inv[i] == null) { return i; }
		}

		for (int i = 35; i >= 9; i--) {
			if (inv[i] == null) { return i; }
		}

		return -1;
	}

	//Take item from crafting
	@SuppressWarnings("deprecation")
	public void TakeItem(InventoryClickEvent event) {
		if (event.getClick() == ClickType.MIDDLE) { return; }

		ItemStack item = event.getCurrentItem();
		if (item.getType() != Material.BOW) { return; }
		if (item.getEnchantmentLevel(Enchantment.MENDING) != 1) { return; }
		if (item.getEnchantmentLevel(Enchantment.ARROW_INFINITE) != 1) { return; }

		//Create mednfinity bow
		ItemStack mendifnity = new ItemStack(Material.BOW);
		mendifnity.addEnchantment(Enchantment.MENDING, 1);
		mendifnity.addEnchantment(Enchantment.ARROW_INFINITE, 1);

		//Add item to inventory if its not empty
		if ((event.getClick() == ClickType.SHIFT_LEFT) || (event.getClick() == ClickType.SHIFT_RIGHT)) {
			Player player = (Player) event.getWhoClicked();
			int EmptySlot = FindEmptySlot(player);

			if (EmptySlot != -1) {
				player.getInventory().setItem(EmptySlot, mendifnity);
				ClearCrafting(event);
			}

			event.setCancelled(true);
			return;
		}

		//Drop item if Q pressed
		if ((event.getClick() == ClickType.DROP) || (event.getClick() == ClickType.CONTROL_DROP)) {
			DropItem((Player) event.getWhoClicked(), mendifnity);
			ClearCrafting(event);
			event.setCancelled(true);
			return;
		}

		event.setCursor(mendifnity);
		ClearCrafting(event);
		event.setCancelled(true);
	}

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[Mendfinity] Mendfinity has loaded!"));
	}

	@EventHandler(priority=EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event)  {
		if (event.getInventory().getType() != InventoryType.WORKBENCH) { return; }
		if (event.getSlot() < 0) { return; }

		//Handle item take
		if ((event.getSlot() == 0) && (event.getSlotType() == SlotType.RESULT)) {
			TakeItem(event);
			return;
		}

		//Check crafting recipe
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
        		CheckCrafting(event.getInventory(), (Player) event.getWhoClicked());
            }
        }, 1L);
	}

	@EventHandler(priority=EventPriority.NORMAL)
    public void onInventoryDrag(InventoryDragEvent event)  {
		if (event.getInventory().getType() != InventoryType.WORKBENCH) { return; }

		//Check crafting recipe
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
        		CheckCrafting(event.getInventory(), (Player) event.getWhoClicked());
            }
        }, 1L);
	}
}