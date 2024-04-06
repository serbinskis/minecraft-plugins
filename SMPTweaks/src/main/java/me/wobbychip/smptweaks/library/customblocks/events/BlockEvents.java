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
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BlockEvents implements Listener {
	public static int DEFAULT_COMPARATOR_DELAY = 2;
	public final CustomBlock customBlock;
	public final HashMap<String, CustomBlock> explodelist = new HashMap<>();
	public final HashMap<String, Block> ticklist = new HashMap<>();
	public final HashMap<String, ItemStack[]> dispenselist = new HashMap<>();
	public boolean busy = false;
	public int busy_task = -1;

	public BlockEvents(CustomBlock customBlock) { this.customBlock = customBlock; }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		String location = Utils.locationToString(event.getLocation().getBlock().getLocation());
		if (!explodelist.containsKey(location)) { return; }
		if (!explodelist.get(location).getId().equalsIgnoreCase(customBlock.getId())) { return; }

		ItemStack itemStack = event.getEntity().getItemStack();
		if (itemStack.getType() != customBlock.getBlockBase()) { return; }
		if (customBlock.isCustomBlock(itemStack)) { return; } //Case, where custom block was inside inventory
		if (customBlock.isMarkedItem(itemStack)) { event.getEntity().setItemStack(customBlock.removeMarkedItem(itemStack)); return; } //Case, where normal block of cblock was inside inventory
		event.getEntity().setItemStack(customBlock.getDropItem(false)); //Case, where dropped block is normal and not marked, this is item of cblock
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplodeEvent(BlockExplodeEvent event) {
		for (Block block : event.blockList()) {
			if (!customBlock.isCustomBlock(block)) { continue; }

			//Add block location to list to process items inside ItemSpawnEvent
			String location = Utils.locationToString(block.getLocation());
			explodelist.put(location, customBlock);
			TaskUtils.scheduleSyncDelayedTask(() -> explodelist.remove(location), 1L);

			customBlock.setMarkedInventory(block);
			customBlock.removeBlock(block);
			customBlock.remove(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (!customBlock.isCustomBlock(event.getBlock())) { return; }

		//This is needed because #isCustomBlock is based on entity
		//And if we remove it here then it will not work inside BlockDropItemEvent
		TaskUtils.scheduleSyncDelayedTask(() -> customBlock.removeBlock(event.getBlock()), 1L);
		customBlock.remove(event.getBlock());

		if (!customBlock.hasInventory()) { return; }
		customBlock.setMarkedInventory(event.getBlock());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockDropItemEvent(BlockDropItemEvent event) {
		if (!customBlock.isCustomBlock(event.getBlock())) { return; }

		for (Item item : event.getItems()) {
			ItemStack itemStack = item.getItemStack();
			if (itemStack.getType() != customBlock.getBlockBase()) { continue; }
			if (customBlock.isCustomBlock(itemStack)) { continue; } //Case, where custom block was inside inventory
			if (customBlock.isMarkedItem(itemStack)) { item.setItemStack(customBlock.removeMarkedItem(itemStack)); continue; } //Case, where normal block of cblock was inside inventory
			item.setItemStack(customBlock.getDropItem(false)); //Case, where dropped block is normal and not marked, this is item of cblock
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (!customBlock.isCustomBlock(event.getItemInHand())) { return; }
		customBlock.createBlock(event.getBlockPlaced(), true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if (busy || !customBlock.isCustomBlock(event.getBlock())) { return; }
		if (customBlock.getDispensable() == CustomBlock.Dispensable.IGNORE) { return; }
		if (customBlock.getDispensable() != CustomBlock.Dispensable.IGNORE) { event.setCancelled(true); }
		if (customBlock.getDispensable() == CustomBlock.Dispensable.DISABLE) { return; }

		Block block = event.getBlock();
		String location = Utils.locationToString(block.getLocation());

		ItemStack[] pitems = dispenselist.remove(location); //Get items before the event inside BlockPhysicsEvent
		if (pitems == null) { return; }

		Inventory inventory = ((org.bukkit.block.Container) block.getState()).getInventory();
		ItemStack[] sitems = inventory.getContents(); //Save items in case if prepareDispense() returns false
		inventory.setContents(pitems); //Set items from physics event for custom dispense

		HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense = new HashMap<>();
		if (!customBlock.prepareDispense(event.getBlock(), dispense)) { inventory.setContents(sitems); return; }

		busy = true;

		for (Map.Entry<ItemStack, Map.Entry<ItemStack, Integer>> drop : dispense.entrySet()) {
			ReflectionUtils.dispenseItem(block, drop.getKey(), drop.getValue().getKey(), drop.getValue().getValue());
		}

		//Set result of custom dispense back to inventory, since even if you cancel event it will still set back old item
		//(FIXED) This 1 tick allows to for hopper to move items which just replaces moved item from mover
		ItemStack[] citems = ((org.bukkit.block.Container) block.getState()).getInventory().getContents();
		busy_task = TaskUtils.scheduleSyncDelayedTask(() -> inventory.setContents(citems), 1L);

		busy = false;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		Block source = event.getInitiator().getLocation().getBlock();
		Block destination = event.getDestination().getLocation().getBlock();

		//Fix for 1 tick gap inside BlockDispenseEvent
		if ((destination.getType() == Material.DISPENSER || destination.getType() == Material.DROPPER) && customBlock.isCustomBlock(destination)) {
			TaskUtils.finishTask(busy_task);
		}

		if (busy || !customBlock.isCustomBlock(source)) { return; }
		if (customBlock.getDispensable() == CustomBlock.Dispensable.IGNORE) { return; }
		if (customBlock.getDispensable() != CustomBlock.Dispensable.IGNORE) { event.setCancelled(true); }
		if (customBlock.getDispensable() == CustomBlock.Dispensable.DISABLE) { return; }

		String location = Utils.locationToString(source.getLocation());
		ItemStack[] pitems = dispenselist.remove(location); //Get items before the event inside BlockPhysicsEvent
		if (pitems == null) { return; }

		Inventory inventory = ((org.bukkit.block.Container) source.getState()).getInventory();
		ItemStack[] sitems = inventory.getContents(); //Save items in case if prepareDispense() returns false
		inventory.setContents(pitems); //Set items from physics event for custom dispense

		HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense = new HashMap<>();
		if (!customBlock.prepareDispense(source, dispense)) { inventory.setContents(sitems); return; }

		busy = true;

		for (Map.Entry<ItemStack, Map.Entry<ItemStack, Integer>> drop : dispense.entrySet()) {
			ReflectionUtils.dispenseItem(source, drop.getKey(), drop.getValue().getKey(), drop.getValue().getValue());
		}

		//Set result of custom dispense back to inventory, since even if you cancel event it will still set back old item
		//(FIXED) This 1 tick allows to for hopper to move items which just replaces moved item from mover
		ItemStack[] citems = ((org.bukkit.block.Container) source.getState()).getInventory().getContents();
		busy_task = TaskUtils.scheduleSyncDelayedTask(() -> inventory.setContents(citems), 1L);

		busy = false;
	}

	//TODO: Not finished, there are still bugs, not also sure about infinite loop in here
	//TODO: Fix issue with comparators not subtracting correctly when two custom emitted are working together
	//TODO: https://i.imgur.com/QZ80X4z.png , https://i.imgur.com/308qB8o.png (KINDA FIXED)
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
		//This is used to save items inside droppers and dispenser before BlockDispenseEvent
		if (event.getChangedType() == Material.DISPENSER || event.getChangedType() == Material.DROPPER) {
			if (!customBlock.isCustomBlock(event.getBlock())) { return; }
			String location = Utils.locationToString(event.getBlock().getLocation());
			ItemStack[] items = ((org.bukkit.block.Container) event.getBlock().getState()).getInventory().getContents();
			for (int i = 0; i < items.length; i++) { items[i] = (items[i] == null) ? null : items[i].clone(); } //Inventory#getContents() returns mirror not clone
			//if (!dispenselist.containsKey(location)) { TaskUtils.scheduleSyncDelayedTask(() -> dispenselist.remove(location), 5L); } //TODO: Possible timings and errors
			dispenselist.put(location, items);
		}

		if ((event.getChangedType() == Material.COMPARATOR) && (customBlock.getComparable() != CustomBlock.Comparable.IGNORE)) {
			BlockFace blockFace = ((Directional) event.getBlock().getBlockData()).getFacing();
			Block customBlock = null;
			Block b1 = event.getBlock().getRelative(blockFace, 1);
			Block b2 = event.getBlock().getRelative(blockFace, 2);
			if (this.customBlock.isCustomBlock(b2) && ReflectionUtils.isRedstoneConductor(b1)) { customBlock = b2; } //Remember comparators can read signal trough block, so we need to check if that block is conductor
			if (this.customBlock.isCustomBlock(b1)) { customBlock = b1; } //Here get custom block behind comparator or block behind comparator
			if (customBlock == null) { return; }

			int power = (this.customBlock.getComparable() != CustomBlock.Comparable.DISABLE) ? this.customBlock.preparePower(customBlock) : 0;
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

			TaskUtils.scheduleSyncDelayedTask(() -> {
				Block block = ticklist.remove(location);
				if (block.getLocation().getBlock().getType() != Material.COMPARATOR) { return; }
				ReflectionUtils.setComparatorPower(block, fpower, false);
				ReflectionUtils.updateNeighborsInFront(block);
			}, DEFAULT_COMPARATOR_DELAY);
		}
	}
}