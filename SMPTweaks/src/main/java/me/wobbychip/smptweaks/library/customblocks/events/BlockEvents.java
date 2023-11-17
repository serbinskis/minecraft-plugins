package me.wobbychip.smptweaks.library.customblocks.events;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.blocks.ComparatorBlock;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.craftbukkit.v1_20_R2.event.CraftEventFactory;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;

public class BlockEvents implements Listener {
	public static int DEFAULT_COMPARATOR_DELAY = 2;
	private final CustomBlock cblock;
	private final HashMap<String, CustomBlock> elocation = new HashMap<>();
	private final HashMap<String, Block> ticklist = new HashMap<>();

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
		if (Main.DEBUG_MODE && (event.getChangedType() != Material.AIR) && (event.getChangedType() != Material.GRASS_BLOCK)) {
			int d1 = ReflectionUtils.getAlternateSignal(event.getBlock());
			Utils.sendMessage(event.getChangedType() + " | " + Utils.locationToString(event.getBlock().getLocation()) + " | input: " + d1);
		}

		if (event.getChangedType() != Material.COMPARATOR) { return; }

		BlockFace blockFace = ((Directional) event.getBlock().getBlockData()).getFacing();
		Block customBlock = null;
		Block b1 = event.getBlock().getRelative(blockFace, 1);
		Block b2 = event.getBlock().getRelative(blockFace, 2);
		if (cblock.isCustomBlock(b2) && ReflectionUtils.isRedstoneConductor(b1)) { customBlock = b2; }
		if (cblock.isCustomBlock(b1)) { customBlock = b1; }
		if (customBlock == null) { return; }

		int power = cblock.preparePower(customBlock);
		if (power < 0) { return; }
		event.setCancelled(true);

		BlockData blockData = ReflectionUtils.getChangedBlockData(event);
		int result_power = getComparatorOutputSignal(event.getBlock(), blockData, power);
		ReflectionUtils.setComparatorPower(event.getBlock(), result_power, false);

		String location = Utils.locationToString(event.getBlock().getLocation());
		if (ticklist.containsKey(location)) { return; }
		ticklist.put(location, event.getBlock());

		TaskUtils.scheduleSyncDelayedTask(new Runnable() {
			public void run() {
				updateNeighborsInFront(ticklist.remove(location));
			}
		}, DEFAULT_COMPARATOR_DELAY);
	}

	public void updateNeighborsInFront(Block block) {
		BlockPos block_pos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel block_world = Objects.requireNonNull(ReflectionUtils.getWorld(block.getWorld()));
		BlockState block_sate = block_world.getBlockState(block_pos);
		net.minecraft.world.level.block.ComparatorBlock block_nms = (net.minecraft.world.level.block.ComparatorBlock) block_sate.getBlock();

		Direction enumdirection = (Direction) block_sate.getValue(DiodeBlock.FACING);
		BlockPos blockposition1 = block_pos.relative(enumdirection.getOpposite());

		block_world.neighborChanged(blockposition1, block_nms, block_pos);
		block_world.updateNeighborsAtExceptFromFacing(blockposition1, block_nms, enumdirection);
	}

	public int getComparatorOutputSignal(Block block, @Nullable BlockData blockData, int power) {
		int j = ReflectionUtils.getAlternateSignal(block);
		if (blockData == null) { blockData = block.getBlockData(); }
		return (j > power ? 0 : (((Comparator) blockData).getMode() == Comparator.Mode.SUBTRACT ? power - j : power));
	}
}