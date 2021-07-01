package me.wobbychip.mendfinity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event)  {
		if (event.getInventory().getType() != InventoryType.WORKBENCH) { return; }
		if (event.getSlot() < 0) { return; }

		//Handle item take
		if ((event.getSlot() == 0) && (event.getSlotType() == SlotType.RESULT) && (event.getClick() != ClickType.MIDDLE)) {
			ItemStack item = event.getCurrentItem();
			if (item.getType() != Material.BOW) { return; }
			if (item.getEnchantmentLevel(Enchantment.MENDING) != 1) { return; }
			if (item.getEnchantmentLevel(Enchantment.ARROW_INFINITE) != 1) { return; }

			//Add item to inventory if its not empty
			if ((event.getClick() == ClickType.SHIFT_LEFT) || (event.getClick() == ClickType.SHIFT_RIGHT)) {
				Player player = (Player) event.getWhoClicked();
				int emptySlot = Utilities.findEmptySlot(player.getInventory());

				if (emptySlot != -1) {
					player.getInventory().setItem(emptySlot, Utilities.mendifnityBow());
					Utilities.clearCrafting(event);
				}

				event.setCancelled(true);
				return;
			}

			//Drop item if Q pressed
			if ((event.getClick() == ClickType.DROP) || (event.getClick() == ClickType.CONTROL_DROP)) {
				Utilities.dropItem((Player) event.getWhoClicked(), Utilities.mendifnityBow());
				Utilities.clearCrafting(event);
				event.setCancelled(true);
				return;
			}

			event.setCursor(Utilities.mendifnityBow());
			Utilities.clearCrafting(event);
			event.setCancelled(true);
			return;
		}

		//Check crafting recipe
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
            public void run() {
            	Utilities.checkCrafting(event.getInventory());
            }
        }, 1L);
	}

	@EventHandler(priority=EventPriority.NORMAL)
    public void onInventoryDrag(InventoryDragEvent event)  {
		if (event.getInventory().getType() != InventoryType.WORKBENCH) { return; }

		//Check crafting recipe
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
            public void run() {
            	Utilities.checkCrafting(event.getInventory());
            }
        }, 1L);
	}

    @EventHandler(priority=EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().discoverRecipe(Main.namespacedKey);
    }
}