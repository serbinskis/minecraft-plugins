package me.wobbychip.smptweaks.custom.autotrade;

import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Traders {
	public static List<ItemStack> handleTrader(Block trader) {
		Inventory source = getSource(trader);
		if (source == null) { return List.of(); }

		Inventory traderInventory = getTraderInventory(trader);
		if (traderInventory == null) { return List.of(); }

		Location location = trader.getLocation().clone().add(0.5, 0.5, 0.5);
		Collection<Entity> villagers = Utils.getNearbyEntities(location, EntityType.VILLAGER, AutoTrade.TRADE_DISTANCE+0.5, false);
		Inventory destination = getDestination(trader);

		for (Entity villager : villagers) {
			Map.Entry<Boolean, List<ItemStack>> result = handleVillager(trader, (Villager) villager, source, destination, traderInventory);
			if (result.getKey()) { return result.getValue(); }
		}

		return List.of();
	}

	public static Map.Entry<Boolean, List<ItemStack>> handleVillager(Block block, Villager villager, Inventory source, Inventory destination, Inventory trader) {
		if (villager.getProfession() == Profession.NONE) { return Map.entry(false, List.of()); }
		if (villager.isTrading() || villager.isSleeping()) { return Map.entry(false, List.of()); }

		Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(AutoTrade.fakePlayer, villager)); //Support global trading tweak

		for (MerchantRecipe recipe : villager.getRecipes()) {
			Map.Entry<Boolean, List<ItemStack>> result = handleTrade(block, villager, recipe, source, destination, trader);
			if (result.getKey()) { return result; }
		}

		return Map.entry(false, List.of());
	}

	public static Map.Entry<Boolean, List<ItemStack>> handleTrade(Block block, Villager villager, MerchantRecipe recipe, Inventory source, Inventory destination, Inventory trader) {
		List<ItemStack> consumeItems = new ArrayList<>();
		List<ItemStack> outputItems = new ArrayList<>();
		getResultItems(villager, recipe, trader, consumeItems, outputItems);
		if (outputItems.isEmpty() || consumeItems.isEmpty()) { return Map.entry(false, List.of()); }

		//If no destination block then just dispense items
		if ((destination == null) && neededExists(source, consumeItems)) {
			if (!Villagers.tradeVillager(block, villager, villager.getRecipes().indexOf(recipe))) { return Map.entry(false, List.of()); }
			removeConsumedItems(source, consumeItems);
			return Map.entry(true, outputItems);
		}

		//Otherwise try to move items to destination
		boolean result = setResultItems(block, villager, recipe, source, destination, consumeItems, outputItems);
		return Map.entry(result, List.of());
    }

	public static Inventory getSource(Block traders) {
		BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) traders.getBlockData()).getFacing();

		BlockState source = new Location(traders.getWorld(),
				traders.getX() + targetFace.getOppositeFace().getModX(),
				traders.getY() + targetFace.getOppositeFace().getModY(),
				traders.getZ() + targetFace.getOppositeFace().getModZ()).getBlock().getState();

		if (!(source instanceof InventoryHolder)) { return null; }
		return ((InventoryHolder) source).getInventory();
	}

	public static Inventory getDestination(Block trader) {
		BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) trader.getBlockData()).getFacing();

		BlockState destination = new Location(trader.getWorld(),
				trader.getX() + targetFace.getModX(),
				trader.getY() + targetFace.getModY(),
				trader.getZ() + targetFace.getModZ()).getBlock().getState();

		if (!(destination instanceof InventoryHolder)) { return null; }
		return ((InventoryHolder) destination).getInventory();
	}

	public static Inventory getTraderInventory(Block trader) {
		Dispenser dispenser = (Dispenser) trader.getState();
		return dispenser.getInventory();
	}

	public static void getResultItems(Villager villager, MerchantRecipe recipe, Inventory trader, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
		if (recipe.getUses() >= recipe.getMaxUses()) { return; }
		if (recipe.getIngredients().isEmpty()) { return; }

		ItemStack result = recipe.getResult();
		if (!trader.containsAtLeast(result, result.getAmount())) { return; }

		ItemStack adjusted = Villagers.adjustItem(villager, recipe, recipe.getIngredients().get(0));
		if (!trader.containsAtLeast(adjusted, adjusted.getAmount())) { return; }

		//Second item should not be adjusted
		ItemStack ingredient = (recipe.getIngredients().size() > 1) ? recipe.getIngredients().get(1) : new ItemStack(Material.AIR);
		if (!ingredient.getType().isAir() && !trader.containsAtLeast(ingredient, ingredient.getAmount())) { return; }

		consumeItems.add(adjusted);
		if (!ingredient.getType().isAir()) { consumeItems.add(ingredient); }
		outputItems.add(result);
	}

	public static boolean neededExists(Inventory source, List<ItemStack> consumeItems) {
		for (ItemStack item : consumeItems) {
			if (!source.containsAtLeast(item, item.getAmount())) { return false; }
		}

		return true;
	}

	public static boolean setResultItems(Block trader, Villager villager, MerchantRecipe recipe, Inventory source, Inventory destination, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
		//Check if needed items exists
		if (!neededExists(source, consumeItems)) { return false; }

		//Trade with villager
		if (!Villagers.tradeVillager(trader, villager, villager.getRecipes().indexOf(recipe))) { return false; }

		//Make a copy to restore in case if we run out of space
		ItemStack[] restore = destination.getContents();

		for (int i = 0; i < restore.length; i++) {
			restore[i] = (restore[i] == null) ? null : restore[i].clone();
		}

		//Add items to destination inventory
		for (ItemStack item : outputItems) {
			Map<Integer, ItemStack> left = destination.addItem(item.clone());
			if (!left.isEmpty()) { destination.setStorageContents(restore); }
			if (!left.isEmpty()) { return false; }
		}

		//Remove items from source inventory
		removeConsumedItems(source, consumeItems);
		return true;
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
}
