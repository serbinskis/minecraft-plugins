package me.wobbychip.smptweaks.library.customblocks.events;

import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomMarker;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.Map;
import java.util.stream.Collectors;

public class WorldEvents implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoadEvent(ChunkLoadEvent event) {
		CustomMarker.collectUnmarkedBlocks(event.getChunk());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntitiesLoadEvent(EntitiesLoadEvent event) {
		CustomMarker.collectUnmarkedBlocks(event.getEntities());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (CustomBlocks.getSize() == 0) { return; }
		event.getPlayer().addResourcePack(CustomBlocks.RESOURCE_PACK_UUID, CustomBlocks.RESOURCE_PACK_URL, CustomBlocks.RESOURCE_PACK_HASH, CustomBlocks.RESOURCE_PACK_PROMPT, true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
		onBlockPistonExtendEvent(new BlockPistonExtendEvent(event.getBlock(), event.getBlocks(), event.getDirection()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
		Map<Block, CustomBlock> blockList = event.getBlocks().stream().filter(CustomBlocks::isCustomBlock).collect(Collectors.toMap(e -> e, CustomBlocks::getCustomBlock));
		blockList.forEach((block, customBlock) -> customBlock.removeBlock(block));

		//For some reason this event is applied later, because runnable execute before blocks are updated
		//And runnable do run in the end or beginning of tick, so this means this operation is delayed
		TaskUtils.scheduleSyncDelayedTask(() -> {
			blockList.forEach((block, customBlock) -> customBlock.createBlock(block.getRelative(event.getDirection()), true));
		}, 2L);
	}
}