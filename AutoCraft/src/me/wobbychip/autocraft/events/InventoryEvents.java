package me.wobbychip.autocraft.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.wobbychip.autocraft.Main;

public class InventoryEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (event.getClickedBlock().getType() != Material.CRAFTING_TABLE) {
			return;
		}

		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		if (!event.getPlayer().isSneaking()) {
			return;
		}

		if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
			return;
		}

		event.setCancelled(true);
		Main.manager.openWorkbench(event.getPlayer(), event.getClickedBlock().getLocation(), InventoryType.WORKBENCH);
	}
}
