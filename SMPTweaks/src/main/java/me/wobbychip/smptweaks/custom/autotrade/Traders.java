package me.wobbychip.smptweaks.custom.autotrade;

import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
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
	public List<Block> traders = new ArrayList<>();

	public void handleTraders() {
		traders = collectTraders();

		for (Block trader : traders) {
			if (!AutoTrade.tweak.getGameRuleBoolean((trader.getWorld()))) { continue; }
			if (!isDisabled(trader)) { handleTrader(trader); };
		}
	}

	public void handleTrader(Block trader) {
		Inventory source = getSource(trader);
		if (source == null) { return; }

		Inventory destination = getDestination(trader);
		if (destination == null) { return; }

		Inventory traderInventory = getTraderInventory(trader);
		if (traderInventory == null) { return; }

		Location location = trader.getLocation().clone().add(0.5, 0.5, 0.5);
		Collection<Entity> villagers = Utils.getNearbyEntities(location, EntityType.VILLAGER, AutoTrade.tradeDistance+0.5, false);

		for (Entity villager : villagers) {
			if (handleVillager(trader, (Villager) villager, source, destination, traderInventory)) { return; }
		}
	}

	public boolean handleVillager(Block block, Villager villager, Inventory source, Inventory destination, Inventory trader) {
		if (villager.getProfession() == Profession.NONE) { return false; }
		if (villager.isTrading() || villager.isSleeping()) { return false; }

		Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(AutoTrade.fakePlayer, villager)); //Support GlobalTrading

		int counter = 0;
		for (MerchantRecipe recipe : villager.getRecipes()) {
			counter = 0;
			while (handleTrade(block, villager, recipe, source, destination, trader)) { counter++; }
			if (counter > 0) { break; }
		}

		return (counter > 0);
	}

	public boolean handleTrade(Block block, Villager villager, MerchantRecipe recipe, Inventory source, Inventory destination, Inventory trader) {
		List<ItemStack> consumeItems = new ArrayList<>();
		List<ItemStack> outputItems = new ArrayList<>();
		getResultItems(villager, recipe, trader, consumeItems, outputItems);

		if ((outputItems.isEmpty()) || (consumeItems.isEmpty())) { return false; }
        return setResultItems(block, villager, recipe, source, destination, consumeItems, outputItems);
    }

	public Inventory getSource(Block traders) {
		BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) traders.getBlockData()).getFacing();

		BlockState source = new Location(traders.getWorld(),
				traders.getX() + targetFace.getOppositeFace().getModX(),
				traders.getY() + targetFace.getOppositeFace().getModY(),
				traders.getZ() + targetFace.getOppositeFace().getModZ()).getBlock().getState();

		if (!(source instanceof InventoryHolder)) { return null; }
		return ((InventoryHolder) source).getInventory();
	}

	public Inventory getDestination(Block trader) {
		BlockFace targetFace = ((org.bukkit.block.data.type.Dispenser) trader.getBlockData()).getFacing();

		BlockState destination = new Location(trader.getWorld(),
				trader.getX() + targetFace.getModX(),
				trader.getY() + targetFace.getModY(),
				trader.getZ() + targetFace.getModZ()).getBlock().getState();

		if (!(destination instanceof InventoryHolder)) { return null; }
		return ((InventoryHolder) destination).getInventory();
	}

	public Inventory getTraderInventory(Block trader) {
		Dispenser dispenser = (Dispenser) trader.getState();
		return dispenser.getInventory();
	}

	public void getResultItems(Villager villager, MerchantRecipe recipe, Inventory trader, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
		if (recipe.getUses() >= recipe.getMaxUses()) { return; }

		ItemStack result = recipe.getResult();
		if (!trader.containsAtLeast(result, result.getAmount())) { return; }

		ItemStack adjusted = Villagers.adjustItem(villager, recipe, recipe.getIngredients().get(0));
		if (!trader.containsAtLeast(adjusted, adjusted.getAmount())) { return; }

		ItemStack ingredient = recipe.getIngredients().get(1); //Second item should not be adjusted
		if (!ingredient.getType().isAir() && !trader.containsAtLeast(ingredient, ingredient.getAmount())) { return; }

		consumeItems.add(adjusted);
		if (!ingredient.getType().isAir()) { consumeItems.add(ingredient); }
		outputItems.add(result);
	}

	public boolean setResultItems(Block trader, Villager villager, MerchantRecipe recipe, Inventory source, Inventory destination, List<ItemStack> consumeItems, List<ItemStack> outputItems) {
		//Check if needed items exists
		for (ItemStack item : consumeItems) {
			if (!source.containsAtLeast(item, item.getAmount())) { return false; }
		}

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

		return true;
	}

	public List<Block> collectTraders() {
		List<Block> traders = new ArrayList<>();

		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				Block trader = getTrader(entity);
				if ((trader != null) && !traders.contains(trader)) { traders.add(trader); }
			}
		}

		return traders;
	}

	public Block getTrader(Entity entity) {
		if (!(entity instanceof ItemFrame)) { return null; }
		if (((ItemFrame) entity).getItem().getType() != Material.NETHER_STAR) { return null; }

		Block trader = entity.getLocation().getBlock().getRelative(((ItemFrame) entity).getAttachedFace());
		if (trader.getType() != Material.DISPENSER) { return null; }
		return trader;
	}

	//THIS IS USED ONLY TO CHECK IF TRADER IS STILL THERE
	public Block getTrader(Block block) {
		Collection<Entity> entities = Utils.getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), null, 1.5, false);

		for (Entity entity : entities) {
			Block trader = getTrader(entity);
			if ((trader != null) && (trader.equals(block))) { return trader; }
		}

		return null;
	}

	public boolean isDisabled(Block block) {
		if (AutoTrade.redstoneMode.equalsIgnoreCase("disabled")) { return false; }
		return ((AutoTrade.redstoneMode.equalsIgnoreCase("indirect") && block.isBlockIndirectlyPowered()) || block.isBlockPowered());
	}

	public boolean isTrader(Block block) {
		return ((block.getType() == Material.DISPENSER) && traders.contains(block));
	}
}
