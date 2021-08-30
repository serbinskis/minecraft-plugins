package me.wobbychip.recallpotion.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.wobbychip.recallpotion.BrewManager;
import me.wobbychip.recallpotion.Main;

public class BlockEvents implements Listener {
	@EventHandler(priority=EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.isCancelled()) { return; }
		if ((event.getBlock() == null) || (event.getBlock().getType() != Material.BREWING_STAND)) { return; }

		BrewManager brewManager = Main.brews.get(event.getBlock().getLocation());
		if (brewManager != null) { brewManager.stop(); }
    }

	@EventHandler(priority=EventPriority.MONITOR)
    public void onBlockExplode(BlockExplodeEvent event) {
		if (event.isCancelled()) { return; }
		if (event.blockList() == null) { return; }

    	for (Block block : event.blockList()) {
    		BrewManager brewManager = Main.brews.get(block.getLocation());
    		if (brewManager != null) { brewManager.stop(); }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
	public void onBlockExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) { return; }
		if (event.blockList() == null) { return; }

    	for (Block block : event.blockList()) {
    		BrewManager brewManager = Main.brews.get(block.getLocation());
    		if (brewManager != null) { brewManager.stop(); }
        }
    }
}