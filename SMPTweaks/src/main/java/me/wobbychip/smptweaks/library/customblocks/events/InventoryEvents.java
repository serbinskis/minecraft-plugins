package me.wobbychip.smptweaks.library.customblocks.events;

import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryEvents implements Listener {
	private final CustomBlock cblock;

	public InventoryEvents(CustomBlock cblock) {
		this.cblock = cblock;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
		World world = event.getInventory().getLocation().getWorld();
		ItemStack result = event.getInventory().getResult();

		if (!cblock.isCustomBlock(result)) { return; }
		event.getInventory().setResult(cblock.prepareCraft(event, world, result) ? result : new ItemStack(Material.AIR));
	}
}