package me.wobbychip.smptweaks.custom.chunkloader.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;

import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;

public class BlockEvents implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplodeEvent(BlockExplodeEvent event) {
		for (Block block : event.blockList()) {
			if (block.getType() != Material.LODESTONE) { continue; }
			ChunkLoader.manager.removeLoader(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType() != Material.LODESTONE) { return; }
		ChunkLoader.manager.removeLoader(event.getBlock());
	}
}