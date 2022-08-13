package me.wobbychip.smptweaks.custom.chunkloader.events;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.utils.Utils;

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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
		Collection<Block> blocks = Utils.getNearbyBlocks(event.getBlock().getLocation(), Material.LODESTONE, 2);
		ChunkLoader.manager.updateLoader(event.getBlock());

		for (Block block : blocks) {
			ChunkLoader.manager.updateLoader(block);
		}
	}
}