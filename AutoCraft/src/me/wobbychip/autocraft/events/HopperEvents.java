package me.wobbychip.autocraft.events;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.autocraft.Main;
import me.wobbychip.autocraft.Utils;
import me.wobbychip.autocraft.crafters.CustomMinecart;

public class HopperEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		if (event.getDestination().getHolder() instanceof StorageMinecart) {
			StorageMinecart storage = (StorageMinecart) event.getDestination().getHolder();
			CustomMinecart minecart = Main.mmanager.get(((Entity) storage).getUniqueId());

			if (minecart != null) {
				if (minecart.addItem(event.getItem())) {
					event.setItem(new ItemStack(Material.AIR));
				} else {
					event.setCancelled(true);
				}
			}
		}

		if (event.getSource().getHolder() instanceof StorageMinecart) {
			StorageMinecart storage = (StorageMinecart) event.getSource().getHolder();
			//Utils.sendMessage(((Entity) storage).toString());
			CustomMinecart minecart = Main.mmanager.get(((Entity) storage).getUniqueId());
			ItemStack item = event.getItem();

			//We are trying to move dummy item, put result in destination inventory
			if ((minecart != null) && Utils.isDummyItem(item)) {
				event.setCancelled(true);
				ItemStack result = minecart.getResult().clone();
				if (result.getType() == Material.AIR) { return; }

	    		if (Utils.canAdd(event.getDestination(), result)) {
	    			event.getDestination().addItem(result);
	    			minecart.craft();
	    		}
			}
		}
	}
}
