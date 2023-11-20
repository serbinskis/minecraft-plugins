package me.wobbychip.smptweaks.library.customblocks.events;

import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockEvents implements Listener {
	public static int DEFAULT_COMPARATOR_DELAY = 2;
	public final CustomBlock cblock;
	public final HashMap<String, CustomBlock> explodelist = new HashMap<>();
	public final HashMap<String, Block> ticklist = new HashMap<>();
	public final HashMap<String, ItemStack[]> dispenselist = new HashMap<>();
	public boolean busy = false;
	public int busy_task = -1;

	public BlockEvents(CustomBlock cblock) {
		this.cblock = cblock;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		String location = Utils.locationToString(event.getLocation().getBlock().getLocation());
		if (!explodelist.containsKey(location)) { return; }
		if (!explodelist.get(location).getName().equalsIgnoreCase(cblock.getName())) { return; }

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
			explodelist.put(location, cblock);
			TaskUtils.scheduleSyncDelayedTask(() -> explodelist.remove(location), 1L);

			cblock.setMarkedInventory(block);
			cblock.removeBlock(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (!cblock.isCustomBlock(event.getBlock())) { return; }

		//This is needed because #isCustomBlock is based on entity
		//And if we remove it here then it will not work inside BlockDropItemEvent
		int task = TaskUtils.scheduleSyncDelayedTask(() -> cblock.removeBlock(event.getBlock()), 1L);

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

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if (busy || !cblock.isCustomBlock(event.getBlock())) { return; }
		if (cblock.getDispensable() == CustomBlock.Dispensable.IGNORE) { return; }
		if (cblock.getDispensable() != CustomBlock.Dispensable.IGNORE) { event.setCancelled(true); }
		if (cblock.getDispensable() == CustomBlock.Dispensable.DISABLE) { return; }

		Block block = event.getBlock();
		String location = Utils.locationToString(block.getLocation());

		ItemStack[] pitems = dispenselist.remove(location); //Get items before the event inside BlockPhysicsEvent
		if (pitems == null) { return; }

		Inventory inventory = ((org.bukkit.block.Container) block.getState()).getInventory();
		ItemStack[] sitems = inventory.getContents(); //Save items in case if prepareDispense() returns false
		inventory.setContents(pitems); //Set items from physics event for custom dispense

		HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense = new HashMap<>();
		if (!cblock.prepareDispense(event.getBlock(), dispense)) { inventory.setContents(sitems); return; }

		busy = true;

		for (Map.Entry<ItemStack, Map.Entry<ItemStack, Integer>> drop : dispense.entrySet()) {
			dispenseItem(block, drop.getKey(), drop.getValue().getKey(), drop.getValue().getValue());
		}

		//Set result of custom dispense back to inventory, since even if you cancel event it will still set back old item
		//(FIXED) This 1 tick allows to for hopper to move items which just replaces moved item from mover
		ItemStack[] citems = ((org.bukkit.block.Container) block.getState()).getInventory().getContents();
		busy_task = TaskUtils.scheduleSyncDelayedTask(() -> inventory.setContents(citems), 1L);

		busy = false;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		Block destination = event.getDestination().getLocation().getBlock();

		//Fix for 1 tick gap inside BlockDispenseEvent
		if ((destination.getType() == Material.DISPENSER || destination.getType() == Material.DROPPER) && cblock.isCustomBlock(destination)) {
			TaskUtils.finishSyncRepeatingTask(busy_task);
		}

		Block source = event.getInitiator().getLocation().getBlock();
		if (!cblock.isCustomBlock(source)) { return; }
		if (cblock.getDispensable() == CustomBlock.Dispensable.IGNORE) { return; }

		//Now manually move items and remove them from slots
	}

	//TODO: Not finished, there are still bugs, not also sure about infinite loop in here
	//TODO: Fix issue with comaprators not substracting correctly when two custom emited are working together
	//TODO: https://i.imgur.com/QZ80X4z.png , https://i.imgur.com/308qB8o.png (KINDA FIXED)
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
		if (event.getChangedType() == Material.DISPENSER || event.getChangedType() == Material.DROPPER) {
			if (!cblock.isCustomBlock(event.getBlock())) { return; }
			String location = Utils.locationToString(event.getBlock().getLocation());
			ItemStack[] items = ((org.bukkit.block.Container) event.getBlock().getState()).getInventory().getContents();
			for (int i = 0; i < items.length; i++) { items[i] = (items[i] == null) ? null : items[i].clone(); } //Inventory#getContents() returns mirror not clone
			//if (!dispenselist.containsKey(location)) { TaskUtils.scheduleSyncDelayedTask(() -> dispenselist.remove(location), 5L); } //TODO: Possible timings and errors
			dispenselist.put(location, items);
		}

		if (event.getChangedType() == Material.COMPARATOR) {
			BlockFace blockFace = ((Directional) event.getBlock().getBlockData()).getFacing();
			Block customBlock = null;
			Block b1 = event.getBlock().getRelative(blockFace, 1);
			Block b2 = event.getBlock().getRelative(blockFace, 2);
			if (cblock.isCustomBlock(b2) && ReflectionUtils.isRedstoneConductor(b1)) { customBlock = b2; } //Remember comparators can read signal trough block, so we need to check if that block is conductor
			if (cblock.isCustomBlock(b1)) { customBlock = b1; } //Here get custom block behind comparator or block behind comparator
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

			TaskUtils.scheduleSyncDelayedTask(() -> {
				Block block = ticklist.remove(location);
				if (block.getLocation().getBlock().getType() != Material.COMPARATOR) { return; }
				ReflectionUtils.setComparatorPower(block, fpower, false);
				ReflectionUtils.updateNeighborsInFront(block);
			}, DEFAULT_COMPARATOR_DELAY);
		}
	}

	//ItemStack to remove can be null which means it will not take any items at all from source inventory and only dispense.
	//If slot is positive it will try to take item from that slot, but if it fails it will take from first available,
	//slot can be also negative, then it will immediately take from first available slot, if item is not found it will (TODO: fail or succeed?)
	public boolean dispenseItem(Block source, ItemStack drop, @Nullable ItemStack remove, int slot) {
		if (source.getType() == Material.DISPENSER) { return dispenseDispenser(source, drop, remove, slot); }
		if (source.getType() == Material.DROPPER) { return dispenseDropper(source, drop, remove, slot); }
		return false;
	}

	//TODO: Should we remove item, if result of dispense is modified item? (Currently: NO)
	private boolean dispenseDispenser(Block source, ItemStack drop, @Nullable ItemStack remove, int slot) {
		if (drop.getAmount() <= 0) { return true; }
		if (source.getType() != Material.DISPENSER) { return false; }
		if (remove == null) { remove = new ItemStack(Material.AIR); }
		if (remove.getType() == Material.AIR) { slot = -1; }
		drop = drop.clone(); remove = remove.clone(); //Make sure drop and remove are not the same item

		BlockPos blockPos = new BlockPos(source.getX(), source.getY(), source.getZ());
		ServerLevel serverLevel = ReflectionUtils.getWorld(source.getLocation().getWorld());
		BlockState blockState = serverLevel.getBlockState(blockPos);
		DispenserBlockEntity tileentitydispenser = (DispenserBlockEntity) serverLevel.getBlockEntity(blockPos, BlockEntityType.DISPENSER).orElse(null);
		BlockSource blockSource = new BlockSource(serverLevel, blockPos, blockState, tileentitydispenser);

		DispenseItemBehavior dispenseItemBehavior = DispenserBlock.DISPENSER_REGISTRY.get(ReflectionUtils.asNMSCopy(drop).getItem());
		net.minecraft.world.item.ItemStack result = dispenseItemBehavior.dispense(blockSource, ReflectionUtils.asNMSCopy(drop));
		if (result.getCount() == drop.getAmount()) { return false; } //It failed to dispense or item was modified inside called event

		org.bukkit.block.Container container = (org.bukkit.block.Container) source.getState();
		remove.setAmount(1); //We always remove by 1 item
		drop.setAmount(result.getCount()); //Update drop amount after dispense

		//IDC, I will not check if item doesn't exist, I will just remove it
		if (slot < 0) {
			Utils.removeItem(container.getInventory(), remove); //Fuck you spigot, can't even make simple method to remove items
		} else {
			ItemStack itemStack = container.getInventory().getItem(slot);
			if (itemStack == null) { itemStack = new ItemStack(Material.AIR); }
			itemStack.setAmount(itemStack.getAmount()-1);
			if (itemStack.getAmount() <= 0) { itemStack = new ItemStack(Material.AIR); }
			container.getInventory().setItem(slot, itemStack);
		}

		//Sadly, but dispense only can dispense by 1 item
		return dispenseDispenser(source, drop, remove, slot);
	}

	private boolean dispenseDropper(Block source, ItemStack drop, ItemStack remove, int slot) {
		BlockPos blockPos = new BlockPos(source.getX(), source.getY(), source.getZ());
		ServerLevel serverLevel = ReflectionUtils.getWorld(source.getLocation().getWorld());
		BlockState blockState = serverLevel.getBlockState(blockPos);

		DispenserBlockEntity tileentitydispenser = (DispenserBlockEntity) serverLevel.getBlockEntity(blockPos, BlockEntityType.DISPENSER).orElse(null);
		BlockSource blockSource = new BlockSource(serverLevel, blockPos, blockState, tileentitydispenser);

		Direction enumdirection = (Direction) serverLevel.getBlockState(blockPos).getValue(DropperBlock.FACING);
		Container iinventory = HopperBlockEntity.getContainerAt(serverLevel, blockPos.relative(enumdirection));
		net.minecraft.world.item.ItemStack result = ReflectionUtils.asNMSCopy(remove);

		BlockFace blockFace = ((Directional) source.getBlockData()).getFacing();
		Block dblock = source.getRelative(blockFace);

		if (dblock.getState() instanceof org.bukkit.block.Container container) {
			Inventory inventory = (dblock.getState() instanceof DoubleChest) ? ((DoubleChest) dblock.getState()).getInventory() : container.getInventory();
			InventoryMoveItemEvent event = new InventoryMoveItemEvent(tileentitydispenser.getOwner().getInventory(), oitemstack.clone(), destinationInventory, true);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) { return false; }

			/*itemstack1 = HopperBlockEntity.addItem(tileentitydispenser, iinventory, CraftItemStack.asNMSCopy(event.getItem()), enumdirection.getOpposite());
			if (event.getItem().equals(oitemstack) && itemstack1.isEmpty()) {
				// CraftBukkit end
				itemstack1 = itemstack.copy();
				itemstack1.shrink(1);
			} else {
				itemstack1 = itemstack.copy();
			}*/
		}

		/*if (iinventory == null) {
			DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior(true);
			result = DISPENSE_BEHAVIOUR.dispense(blockSource, ReflectionUtils.asNMSCopy(drop));
		} else {
			CraftItemStack oitemstack = CraftItemStack.asCraftMirror(itemstack.copy().split(1));

			org.bukkit.inventory.Inventory destinationInventory;
			// Have to special case large chests as they work oddly
			if (iinventory instanceof CompoundContainer) {
				destinationInventory = new org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventoryDoubleChest((CompoundContainer) iinventory);
			} else {
				destinationInventory = iinventory.getOwner().getInventory();
			}

			InventoryMoveItemEvent event = new InventoryMoveItemEvent(tileentitydispenser.getOwner().getInventory(), oitemstack.clone(), destinationInventory, true);
			serverLevel.getCraftServer().getPluginManager().callEvent(event);
			if (event.isCancelled()) { return; }
			result = HopperBlockEntity.addItem(tileentitydispenser, iinventory, CraftItemStack.asNMSCopy(event.getItem()), enumdirection.getOpposite());
			if (event.getItem().equals(oitemstack) && result.isEmpty()) {
				result = itemstack.copy();
				result.shrink(1);
			} else {
				result = itemstack.copy();
			}
		}*/

		return true;
	}
}