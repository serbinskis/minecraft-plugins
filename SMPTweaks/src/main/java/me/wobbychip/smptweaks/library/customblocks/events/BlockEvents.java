package me.wobbychip.smptweaks.library.customblocks.events;

import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BlockEvents implements Listener {
	private final CustomBlock cblock;
	private final HashMap<String, CustomBlock> elocation = new HashMap<>();
	private boolean busy = false;

	public BlockEvents(CustomBlock cblock) {
		this.cblock = cblock;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		String location = Utils.locationToString(event.getLocation().getBlock().getLocation());
		if (!elocation.containsKey(location)) { return; }
		if (!elocation.get(location).getName().equalsIgnoreCase(cblock.getName())) { return; }

		ItemStack itemStack = event.getEntity().getItemStack();
		if (itemStack.getType() != cblock.getBlockBase()) { return; }
		if (cblock.isCustomBlock(itemStack)) { return; } //Case, where custom block was inside inventory
		if (cblock.isMarkedItem(itemStack)) { event.getEntity().setItemStack(cblock.removeMarkedItem(itemStack)); return; } //Case, where normal block of cblock was inside inventory
		event.getEntity().setItemStack(cblock.getDropItem()); //Case, where dropped block is normal and not marked, this is item of cblock
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplodeEvent(BlockExplodeEvent event) {
		for (Block block : event.blockList()) {
			if (!cblock.isCustomBlock(block)) { continue; }

			//Add block location to list to process items inside ItemSpawnEvent
			String location = Utils.locationToString(block.getLocation());
			elocation.put(location, cblock);

			TaskUtils.scheduleSyncDelayedTask(new Runnable() {
				public void run() { elocation.remove(location); }
			}, 1L);

			cblock.setMarkedInventory(block);
			cblock.removeBlock(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (!cblock.isCustomBlock(event.getBlock())) { return; }

		//This is needed because #isCustomBlock is based on entity
		//And if we remove it here then it will not work inside BlockDropItemEvent
		int task = TaskUtils.scheduleSyncDelayedTask(new Runnable() {
			public void run() { cblock.removeBlock(event.getBlock()); }
		}, 1L);

		if (!cblock.hasInventory()) { return; }
		cblock.setMarkedInventory(event.getBlock());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockDropItemEvent(BlockDropItemEvent event) {
		if (!cblock.isCustomBlock(event.getBlock())) { return; }

		for (Item item : event.getItems()) {
			ItemStack itemStack = item.getItemStack();
			if (itemStack.getType() != cblock.getBlockBase()) { continue; }
			if (cblock.isCustomBlock(itemStack)) { continue; } //Case, where custom block was inside inventory
			if (cblock.isMarkedItem(itemStack)) { item.setItemStack(cblock.removeMarkedItem(itemStack)); continue; } //Case, where normal block of cblock was inside inventory
			item.setItemStack(cblock.getDropItem()); //Case, where dropped block is normal and not marked, this is item of cblock
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (!cblock.isCustomBlock(event.getItemInHand())) { return; }
		cblock.createBlock(event.getBlockPlaced());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
		if (busy || (event.getChangedType() != Material.COMPARATOR)) { return; }

		BlockFace blockFace = ((Directional) event.getBlock().getBlockData()).getFacing();
		BlockFace oppositeFace = ((Directional) event.getBlock().getBlockData()).getFacing().getOppositeFace();
		Block customBlock = null;
		Block b1 = event.getBlock().getRelative(blockFace, 1);
		Block b2 = event.getBlock().getRelative(blockFace, 2);
		if (cblock.isCustomBlock(b2) && !b1.getType().isTransparent()) { customBlock = b2; }
		if (cblock.isCustomBlock(b1)) { customBlock = b1; }
		if (customBlock == null) { return; }

		int power = cblock.preparePower(customBlock);
		if (power < 0) { return; }
		event.setCancelled(true);

		//TODO: calculate substraction power
		//((Comparator) blockData).getMode()
		//Need a way to get block NBT

		busy = true;
		BlockData blockData = ReflectionUtils.getChangedBlockData(event);
		((Comparator) blockData).setPowered(power > 0);
		event.getBlock().setBlockData(blockData, false); //Update block without triggering physics event
		ReflectionUtils.setBlockNbt(event.getBlock().getLocation().getBlock(), "OutputSignal", power, false);
		ReflectionUtils.forceUpdateBlock(event.getBlock().getRelative(oppositeFace, 1));
		ReflectionUtils.forceUpdateBlock(event.getBlock().getRelative(oppositeFace, 2));
		busy = false;
	}
}