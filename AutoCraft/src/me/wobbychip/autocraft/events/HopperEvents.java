package me.wobbychip.autocraft.events;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.autocraft.Main;
import me.wobbychip.autocraft.Utils;
import me.wobbychip.autocraft.crafters.CustomMinecart;

public class HopperEvents implements Listener {
	//Minecart with hoppers cannot put stuff inside minecarft with chest only take out
	//That kinda wird but ok, don't know any other way to fix it

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		if (event.getDestination().getHolder() instanceof StorageMinecart) {
			StorageMinecart storage = (StorageMinecart) event.getDestination().getHolder();
			CustomMinecart minecart = Main.mmanager.get(((Entity) storage).getUniqueId());

			if (minecart != null) {
				if (minecart.getCrafting().addItem(event.getItem())) {
					if (event.getSource().getType() == InventoryType.HOPPER) {
						event.setItem(new ItemStack(Material.AIR));
					} else if (event.getSource().getType() == InventoryType.DROPPER) {
						event.getSource().removeItem(event.getItem());
						event.setCancelled(true);
					} else {
						event.setCancelled(true);
					}
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
				ItemStack result = minecart.getCrafting().getResult().clone();
				if (result.getType() == Material.AIR) { return; }

	    		if (Utils.canAdd(event.getDestination(), result)) {
	    			event.getDestination().addItem(result);
	    			minecart.getCrafting().craft();
	    		}
			}
		}
	}
}
