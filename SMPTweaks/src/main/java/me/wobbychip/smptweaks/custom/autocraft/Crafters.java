package me.wobbychip.smptweaks.custom.autocraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;

public class Crafters {
	public static List<ItemStack> handleCrafter(Block crafter) {
		Inventory source = getSource(crafter);
		if (source == null) { return List.of(); }

		List<ItemStack> crafterItems = getCrafterItems(crafter);
		if (crafterItems == null) { return List.of(); }

		List<ItemStack> consumeItems = new ArrayList<>();
		List<ItemStack> outputItems = new ArrayList<>();
		getResultItems(crafterItems, consumeItems, outputItems);
		if (outputItems.isEmpty() || consumeItems.isEmpty()) { return List.of(); }

		Inventory destination = getDestination(crafter);
		if (destination == null) { removeConsumedItems(source, consumeItems); return outputItems; }

		setResultItems(source, destination, consumeItems, outputItems);
		return List.of();
    }

	public static Inventory getSource(Block crafter) {
		BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) crafter.getBlockData()).getFacing();

		BlockState source = new Location(crafter.getWorld(),
				crafter.getX() + targetFace.getOppositeFace().getModX(),
				crafter.getY() + targetFace.getOppositeFace().getModY(),
				crafter.getZ() + targetFace.getOppositeFace().getModZ()).getBlock().getState();

		if (!(source instanceof InventoryHolder)) { return null; }
		return ((InventoryHolder) source).getInventory();
	}

	public static Inventory getDestination(Block crafter) {
		BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) crafter.getBlockData()).getFacing();

		BlockState destination = new Location(crafter.getWorld(),
				crafter.getX() + targetFace.getModX(),
				crafter.getY() + targetFace.getModY(),
				crafter.getZ() + targetFace.getModZ()).getBlock().getState();

		if (!(destination instanceof InventoryHolder)) { return null; }
		return ((InventoryHolder) destination).getInventory();
	}

	public static List<ItemStack> getCrafterItems(Block crafter) {
		Dispenser dispenser = (Dispenser) crafter.getState();
		List<ItemStack> items = new ArrayList<>(Arrays.asList(dispenser.getInventory().getContents()));
		if (items.stream().noneMatch(Objects::nonNull)) { return null; }
		return items;
	}

	public static void getResultItems(List<ItemStack> crafterItems, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
		//Get crafting result from list of items and add to output
		ItemStack result = getCraftResult(crafterItems);
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

	public static void setResultItems(Inventory source, Inventory destination, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
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

		removeConsumedItems(source, consumeItems);
	}

	public static void removeConsumedItems(Inventory source, List<ItemStack> consumeItems) {
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

	public static ItemStack getCraftResult(List<ItemStack> items) {
		if (items.size() != 9) { return null; }
		ItemStack[] craftingItems = new ItemStack[9];

		for (int i = 0; i < items.size(); i++) {
			craftingItems[i] = (items.get(i) == null) ? new ItemStack(Material.AIR) : items.get(i);
		}

		Recipe recipe = Bukkit.getCraftingRecipe(craftingItems, Bukkit.getWorlds().get(0));
		if ((recipe == null) || recipe.getResult().getType().isAir()) { return null; }

		ItemStack result = recipe.getResult();
		if (result.getType() == Material.FIREWORK_ROCKET) { getFireworkResult(Arrays.asList(craftingItems), result); }
		return result;
	}

	public static void getFireworkResult(List<ItemStack> items, ItemStack result) {
		ItemStack gunpowder = new ItemStack(Material.GUNPOWDER);
		int count = (int) items.stream().filter(i -> i.isSimilar(gunpowder)).count();

		FireworkMeta fireworkMeta = (FireworkMeta) result.getItemMeta();
		fireworkMeta.setPower((count <= 3 && count >= 1) ? count : 1);
		if ((count <= 3 && count >= 1)) { result.setAmount(3); }
		result.setItemMeta(fireworkMeta);
	}
}
