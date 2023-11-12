package me.wobbychip.smptweaks.custom.autocraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Crafters {
	public List<Block> crafters = new ArrayList<>();

	public void handleCrafters() {
		crafters = collectCrafters();

		for (Block crafter : crafters) {
			if (!AutoCraft.tweak.getGameRuleBoolean((crafter.getWorld()))) { continue; }
			if (!isDisabled(crafter)) { handleCrafter(crafter); };
		}
	}

	public void handleCrafter(Block crafter) {
		Inventory source = getSource(crafter);
		if (source == null) { return; }

		Inventory destination = getDestination(crafter);
		if (destination == null) { return; }

		List<ItemStack> crafterItems = getCrafterItems(crafter);
		if (crafterItems == null) { return; }

		List<ItemStack> consumeItems = new ArrayList<>();
		List<ItemStack> outputItems = new ArrayList<>();
		getResultItems(crafterItems, consumeItems, outputItems);

		if ((outputItems.size() == 0) || (consumeItems.size() == 0)) { return; }
		setResultItems(source, destination, consumeItems, outputItems);
	}

	public Inventory getSource(Block crafter) {
		BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) crafter.getBlockData()).getFacing();

		BlockState source = new Location(crafter.getWorld(),
				crafter.getX() + targetFace.getOppositeFace().getModX(),
				crafter.getY() + targetFace.getOppositeFace().getModY(),
				crafter.getZ() + targetFace.getOppositeFace().getModZ()).getBlock().getState();

		if (!(source instanceof InventoryHolder)) { return null; }
		return ((InventoryHolder) source).getInventory();
	}

	public Inventory getDestination(Block crafter) {
		BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) crafter.getBlockData()).getFacing();

		BlockState destination = new Location(crafter.getWorld(),
				crafter.getX() + targetFace.getModX(),
				crafter.getY() + targetFace.getModY(),
				crafter.getZ() + targetFace.getModZ()).getBlock().getState();

		if (!(destination instanceof InventoryHolder)) { return null; }
		return ((InventoryHolder) destination).getInventory();
	}

	public List<ItemStack> getCrafterItems(Block crafter) {
		Dispenser dispenser = (Dispenser) crafter.getState();
		List<ItemStack> items = new ArrayList<>(Arrays.asList(dispenser.getInventory().getContents()));
		if (items.stream().noneMatch(Objects::nonNull)) { return null; }
		return items;
	}

	public void getResultItems(List<ItemStack> crafterItems, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
		//Get crafting result from list of items and add to output
		ItemStack result = Recipes.getCraftResult(crafterItems);
		if (result == null) { return; }
		outputItems.add(result);

		//Remove null items
		crafterItems.removeIf(Objects::isNull);

		//Count items to consume and items to give back (e.g. milk bucket -> bucket)
		for (ItemStack item : crafterItems) {
			if (consumeItems.stream().noneMatch(i -> i.isSimilar(item))) {
				int count = (int) crafterItems.stream().filter(i -> i.isSimilar(item)).count();

				ItemStack consume = new ItemStack(item);
				consume.setAmount(count);
				consumeItems.add(consume);

				Material remaining = item.getType().getCraftingRemainingItem();
				if ((remaining != null) && !remaining.isAir()) { outputItems.add(new ItemStack(remaining, count)); }
			}
		}
	}

	public void setResultItems(Inventory source, Inventory destination, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
		//Check if needed items exists
		for (ItemStack item : consumeItems) {
			if (!source.containsAtLeast(item, item.getAmount())) { return; }
		}

		//Make a copy to restore in case if we run out of space
		ItemStack[] restore = destination.getContents();

		for (int i = 0; i < restore.length; i++) {
			restore[i] = (restore[i] == null) ? null : restore[i].clone();
		}

		//Add items to destination inventory
		for (ItemStack item : outputItems) {
			Map<Integer, ItemStack> left = destination.addItem(item.clone());
			if (!left.isEmpty()) { destination.setStorageContents(restore); }
			if (!left.isEmpty()) { return; }
		}

		//Remove items from source inventory
		for (ItemStack item : consumeItems) {
			for (int i = 0; i < item.getAmount(); i++) {
				for (ItemStack sourceItem : source.getContents()) {
					if ((sourceItem != null) && sourceItem.isSimilar(item)) {
						sourceItem.setAmount(sourceItem.getAmount()-1);
						break;
					}
				}
			}
		}
	}

	public List<Block> collectCrafters() {
		List<Block> crafters = new ArrayList<>();

		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				Block crafter = getCrafter(entity);
				if ((crafter != null) && !crafters.contains(crafter)) { crafters.add(crafter); }
			}
		}

		return crafters;
	}

	public Block getCrafter(Entity entity) {
		if (!(entity instanceof ItemFrame)) { return null; }
		if (((ItemFrame) entity).getItem().getType() != Material.CRAFTING_TABLE) { return null; }

		Block crafter = entity.getLocation().getBlock().getRelative(((ItemFrame) entity).getAttachedFace());
		if (crafter.getType() != Material.DISPENSER) { return null; }
		return crafter;
	}

	public boolean isDisabled(Block block) {
		if (AutoCraft.redstoneMode.equalsIgnoreCase("disabled")) { return false; }
		return ((AutoCraft.redstoneMode.equalsIgnoreCase("indirect") && block.isBlockIndirectlyPowered()) || block.isBlockPowered());
	}

	public boolean isCrafter(Block block) {
		return ((block.getType() == Material.DISPENSER) && crafters.contains(block));
	}
}
