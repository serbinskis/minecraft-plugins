package me.wobbychip.smptweaks.library.customblocks.events;

import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public record InventoryEvents(CustomBlock customBlock) implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
		World world = event.getInventory().getLocation().getWorld();
		ItemStack result = event.getInventory().getResult();

		if (!customBlock.isCustomBlock(result)) { return; }
		event.getInventory().setResult(customBlock.prepareCraft(event, world, result) ? result : new ItemStack(Material.AIR));
	}
}