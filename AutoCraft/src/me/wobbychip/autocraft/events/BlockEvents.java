package me.wobbychip.autocraft.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.wobbychip.autocraft.InventoryManager;
import me.wobbychip.autocraft.Utilities;

public class BlockEvents implements Listener {
	@EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getBlock().getType() != Material.CRAFTING_TABLE) { return; }

		InventoryManager inventoryManager = Utilities.getInventoryManager(event.getBlock().getLocation());
		inventoryManager.destroyInventory();
	}

    @EventHandler(priority=EventPriority.MONITOR)
	public void onBlockExplode(EntityExplodeEvent event) {
    	for (Block block : event.blockList()) {
    		if (block.getType() == Material.CRAFTING_TABLE) {
    			InventoryManager inventoryManager = Utilities.getInventoryManager(block.getLocation());
    			inventoryManager.destroyInventory();
    		}
        }
    }

	@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if (event.getClickedBlock().getType() != Material.CRAFTING_TABLE) { return; }

		event.setCancelled(true);
		if (event.getHand() != EquipmentSlot.HAND) { return; }

		InventoryManager inventoryManager = Utilities.getInventoryManager(event.getClickedBlock().getLocation());
		inventoryManager.openInventory(event.getPlayer());
	}
}
