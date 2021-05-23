package me.wobbychip.workbenchinventory.events;

import java.io.File;
import java.io.IOException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import me.wobbychip.workbenchinventory.Main;
import me.wobbychip.workbenchinventory.Utilities;

public class InventoryEvents implements Listener {
	@EventHandler(priority=EventPriority.NORMAL)
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		Inventory inv = event.getInventory();
		if (inv == null) { return; }
		if (inv.getType() != InventoryType.WORKBENCH) { return; }

		String uuid = event.getPlayer().getUniqueId().toString();
		File file = new File(Main.plugin.getDataFolder(), uuid + ".bin");
		byte[] bytes = Utilities.readFile(file);
		if (bytes.length <= 0) { return; }

		try {
			Utilities.inventoryFromByteArray(inv, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		if (inv == null) { return; }
		if (inv.getType() != InventoryType.WORKBENCH) { return; }

		String uuid = event.getPlayer().getUniqueId().toString();
		File file = new File(Main.plugin.getDataFolder(), uuid + ".bin");
		byte[] bytes = Utilities.inventoryToByteArray(inv);
		Utilities.writeFile(file, bytes);

		inv.clear();
	}
}
