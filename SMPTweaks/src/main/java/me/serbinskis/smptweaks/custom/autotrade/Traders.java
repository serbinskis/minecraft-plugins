package me.serbinskis.smptweaks.custom.autotrade;

import me.serbinskis.smptweaks.custom.autotrade.blocks.TraderBlock;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.utils.VillagerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.*;

public class Traders {
	public static List<ItemStack> handleTrader(Block trader) {
		Inventory source = getTraderInventory(trader);
		if (source == null) { return List.of(); }

		Location location = trader.getLocation().clone().add(0.5, 0.5, 0.5);
		Collection<Entity> villagers = Utils.getNearbyEntities(location, EntityType.VILLAGER, AutoTrade.TRADE_DISTANCE+0.5, false);
		Inventory destination = getDestination(trader);

		for (Entity villager : villagers) {
			Map.Entry<Boolean, List<ItemStack>> result = handleVillager(trader, (Villager) villager, source, destination);
			if (result.getKey()) { return result.getValue(); }
		}

		return List.of();
	}

	public static Map.Entry<Boolean, List<ItemStack>> handleVillager(Block block, Villager villager, Inventory source, Inventory destination) {
		if (villager.getProfession() == Profession.NONE) { return Map.entry(false, List.of()); }
		if (villager.isTrading() || villager.isSleeping()) { return Map.entry(false, List.of()); }

		Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(AutoTrade.fakePlayer, villager)); //Support for global trading tweak

		for (MerchantRecipe recipe : villager.getRecipes()) {
			Map.Entry<Boolean, List<ItemStack>> result = handleTrade(block, villager, recipe, source, destination);
			if (result.getKey()) { return result; }
		}

		return Map.entry(false, List.of());
	}

	public static Map.Entry<Boolean, List<ItemStack>> handleTrade(Block trader, Villager villager, MerchantRecipe recipe, Inventory source, Inventory destination) {
		List<ItemStack> consumeItems = new ArrayList<>();
		List<ItemStack> outputItems = new ArrayList<>();

		MerchantRecipe merchantRecipe = TraderBlock.getMerchantRecipe(trader);
		if (merchantRecipe == null) { return Map.entry(false, List.of()); }

		Inventory recipeInventory = Bukkit.createInventory(null, InventoryType.DISPENSER);
        recipeInventory.addItem(merchantRecipe.getIngredients().toArray(ItemStack[]::new));
        recipeInventory.addItem(merchantRecipe.getResult());

        getResultItems(villager, recipe, recipeInventory, consumeItems, outputItems);
		if (outputItems.isEmpty() || consumeItems.isEmpty()) { return Map.entry(false, List.of()); }

		//If no destination block then just dispense items
		if ((destination == null) && neededExists(source, consumeItems)) {
			if (!Traders.tradeVillager(trader, villager, villager.getRecipes().indexOf(recipe))) { return Map.entry(false, List.of()); }
			removeConsumedItems(source, consumeItems);
			return Map.entry(true, outputItems);
		}

		//Otherwise try to move items to destination
		boolean result = setResultItems(trader, villager, recipe, source, destination, consumeItems, outputItems);
		return Map.entry(result, List.of());
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
		if (!(trader.getState() instanceof Dispenser dispenser)) { return null; }
		return dispenser.getInventory();
	}

	public static void getResultItems(Villager villager, MerchantRecipe recipe, Inventory trader, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
		if (recipe.getUses() >= recipe.getMaxUses()) { return; }
		if (recipe.getIngredients().isEmpty()) { return; }

		ItemStack result = recipe.getResult();
		if (!trader.containsAtLeast(result, result.getAmount())) { return; }

		ItemStack adjusted = VillagerUtils.adjustItem(villager, AutoTrade.fakePlayer, recipe, recipe.getIngredients().get(0));
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
		if (!Traders.tradeVillager(trader, villager, villager.getRecipes().indexOf(recipe))) { return false; }

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

	public static boolean tradeVillager(Block trader, Villager villager, int trade) {
		if (!VillagerUtils.canBuy(AutoTrade.fakePlayer, villager, trade)) { return false; }

		for (ItemStack item : villager.getRecipes().get(trade).getIngredients()) {
			item.setAmount(item.getMaxStackSize());
			AutoTrade.fakePlayer.getInventory().addItem(item);
		}

		AutoTrade.fakePlayer.openMerchant(villager, true);
		ReflectionUtils.selectTrade(AutoTrade.fakePlayer, trade);
		ReflectionUtils.quickMoveStack(AutoTrade.fakePlayer, 2);

		AutoTrade.fakePlayer.closeInventory();
		AutoTrade.fakePlayer.getInventory().clear();
		AutoTrade.fakePlayer.setTotalExperience(0);

		//FUCK THE BUKKIT AGAIN, EntitySpawnEvent not working with xp
		for (Entity entity : villager.getLocation().getWorld().getNearbyEntities(villager.getLocation(), 1, 1, 1)) {
			if (!(entity instanceof ExperienceOrb)) { continue; }
			if (storeOrb((ExperienceOrb) entity, villager, trader)) { break; }
		}

		return true;
	}

	public static boolean storeOrb(ExperienceOrb orb, Villager villager, Block block) {
		for (Entity entity : orb.getLocation().getWorld().getNearbyEntities(orb.getLocation(), 0.01, 0.01, 0.01)) {
			if (!entity.getUniqueId().equals(villager.getUniqueId())) { continue; }
			TraderBlock.storeXp(block, orb.getExperience());
			orb.remove();
			return true;
		}

		return false;
	}
}
