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
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BlockEvents implements Listener {
	public static int DEFAULT_COMPARATOR_DELAY = 2;
	public final CustomBlock cblock;
	public final HashMap<String, CustomBlock> elocation = new HashMap<>();
	public final HashMap<String, Block> ticklist = new HashMap<>();

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

	//TODO: Not finished, there are still bugs, not also sure about infinite loop in here
	//TODO: Fix issue with comaprators not substracting correctly when two custom emited are working together
	//TODO: https://i.imgur.com/QZ80X4z.png , https://i.imgur.com/308qB8o.png (KINDA FIXED)
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
		if (event.getChangedType() != Material.COMPARATOR) { return; }
		//if (Main.DEBUG_MODE) { Utils.sendMessage(event.getChangedType() + " | " + Utils.locationToString(event.getBlock().getLocation()) + " | input: " + ReflectionUtils.getAlternateSignal(event.getBlock())); }

		//Here get custom block behind comparator or block behind comparator
		//Remember comparators can read signal trough block, so we need to check if that block is conductor

		BlockFace blockFace = ((Directional) event.getBlock().getBlockData()).getFacing();
		Block customBlock = null;
		Block b1 = event.getBlock().getRelative(blockFace, 1);
		Block b2 = event.getBlock().getRelative(blockFace, 2);
		if (cblock.isCustomBlock(b2) && ReflectionUtils.isRedstoneConductor(b1)) { customBlock = b2; }
		if (cblock.isCustomBlock(b1)) { customBlock = b1; }
		if (customBlock == null) { return; }

		int power = cblock.preparePower(customBlock);
		if (power < 0) { return; } else { event.setCancelled(true); }

		//Cancelling event only cancels further block update around, it doesn't prevent block state changing
		//But those other blocks can trigger block update without BlockPhysicsEvent, like when getting output signal it updates and saves it

		BlockData blockData = ReflectionUtils.getChangedBlockData(event);
		final int fpower = ReflectionUtils.getComparatorOutputSignal(event.getBlock(), blockData, power);
		ReflectionUtils.setComparatorPower(event.getBlock(), fpower, false);

		String location = Utils.locationToString(event.getBlock().getLocation());
		if (ticklist.containsKey(location)) { return; } //Prevent infinite loop like this: https://i.imgur.com/KQcgFqq.png
		ticklist.put(location, event.getBlock());

		//By default, after the BlockPhysicsEvent it runs blockEntity.getBlockState().neighborChanged() which
		//by default for comparator schedules block tick after 2 (default) ticks, which on its own recalculates
		//output signal and saves it. So in order to prevent it, we cancel the event and schedule update manually,
		//and then set data again and update blocks in the front with DiodeBlock#updateNeighborsInFront();

		//But, big but, there are still some minor bugs with timings and updates, which I don't know how to fix,
		//and kind of don't want to, because currently it fulfills my needs. For example, there are some bugs with
		//user interaction, like, when switching modes on comparator from side.

		TaskUtils.scheduleSyncDelayedTask(new Runnable() {
			public void run() {
				Block block = ticklist.remove(location);
				if (block.getLocation().getBlock().getType() != Material.COMPARATOR) { return; }
				ReflectionUtils.setComparatorPower(block, fpower, false);
				ReflectionUtils.updateNeighborsInFront(block);
			}
		}, DEFAULT_COMPARATOR_DELAY);
	}
}