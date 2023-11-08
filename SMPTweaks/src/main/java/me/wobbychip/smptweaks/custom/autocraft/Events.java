package me.wobbychip.smptweaks.custom.autocraft;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if (AutoCraft.crafters.isCrafter(event.getBlock())) { event.setCancelled(true); }
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		if (AutoCraft.allowBlockRecipeModification) { return; }

		InventoryType type = event.getInitiator().getType();
		if ((type != InventoryType.HOPPER) && (type != InventoryType.DROPPER)) { return; }

		Block source = event.getSource().getLocation().getBlock();
		if (AutoCraft.crafters.isCrafter(source)) { event.setCancelled(true); }
		if (event.isCancelled()) { return; } //Prevent further unnecessary code execution

		Block destination = event.getDestination().getLocation().getBlock();
		if (AutoCraft.crafters.isCrafter(destination)) { event.setCancelled(true); }
	}
}