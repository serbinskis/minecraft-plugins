package me.wobbychip.autocraft.events;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import me.wobbychip.autocraft.InventoryManager;
import me.wobbychip.autocraft.Main;
import me.wobbychip.autocraft.Utilities;

public class InventoryEvents implements Listener {
	@EventHandler(priority=EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		if (inv == null) { return; }
		if (!(inv.getHolder() instanceof InventoryManager)) { return; }

		InventoryManager inventoryManager = (InventoryManager) inv.getHolder();
		if ((!inventoryManager.isDestroyed) && (inventoryManager.getViewers() <= 1)) {
			inventoryManager.Save();
		}

		Utilities.DebugInfo("onInventoryClose");
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onInventoryClick(InventoryClickEvent event) {
		Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				for (HumanEntity humanEntity : event.getInventory().getViewers()) {
					Player player = (Player) humanEntity;
					player.updateInventory();
			    }
			}
		}, 1L);
	}
}
