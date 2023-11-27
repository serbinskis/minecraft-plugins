package me.wobbychip.smptweaks.custom.custompotions.events;

import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBrew(BrewEvent event) {
		boolean gameRule = CustomPotions.tweak.getGameRuleBoolean(event.getBlock().getWorld());

		for (int i = 0; i < 3; i++) {
			ItemStack item = event.getResults().get(i);
			CustomPotion customPotion = CustomPotions.manager.getCustomPotion(item);
			if ((customPotion != null) && !gameRule) { event.getResults().set(i, customPotion.getDisabledPotion(item)); }
			if ((customPotion != null) && gameRule) { event.getResults().set(i, customPotion.setProperties(item)); }
		}

		//Because potion tag is lost after the event, we need to update it in the next tick
		TaskUtils.scheduleSyncDelayedTask(() -> {
			for (int i = 0; i < 3; i++) {
				ItemStack item = event.getContents().getItem(i);
				CustomPotion customPotion = CustomPotions.manager.getCustomPotion(item);
				if ((customPotion != null) && !gameRule) { event.getResults().set(i, customPotion.getDisabledPotion(item)); }
				if ((customPotion != null) && gameRule) { event.getContents().setItem(i, customPotion.setProperties(item)); }
			}
		}, 1L);
	}

	//Fix potion tag, cuz bukkit is trash and don't support custom potion tag
	//And prevent disenchanting custom potions in grindstone
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getView().getTopInventory() instanceof BrewerInventory) {
			CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getCurrentItem());
			if (customPotion != null) { event.setCurrentItem(customPotion.setPotionTag(event.getCurrentItem())); }
		}

		if (event.getView().getTopInventory() instanceof GrindstoneInventory) {
			TaskUtils.scheduleSyncDelayedTask(() -> {
				Inventory inv = event.getView().getTopInventory();
				CustomPotion customPotion = CustomPotions.manager.getCustomPotion(inv.getItem(2));
				if (customPotion != null) { inv.setItem(2, new ItemStack(Material.AIR)); }
			}, 1L);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getView().getTopInventory() instanceof BrewerInventory) {
			TaskUtils.scheduleSyncDelayedTask(() -> {
				Inventory inv = event.getView().getTopInventory();
				for (int i = 0; i < inv.getSize(); i++) {
					CustomPotion customPotion = CustomPotions.manager.getCustomPotion(inv.getItem(i));
					if (customPotion != null) { inv.setItem(i, customPotion.setPotionTag(inv.getItem(i))); }
				}
			}, 1L);
		}

		if (event.getView().getTopInventory() instanceof GrindstoneInventory) {
			TaskUtils.scheduleSyncDelayedTask(() -> {
				Inventory inv = event.getView().getTopInventory();
				CustomPotion customPotion = CustomPotions.manager.getCustomPotion(inv.getItem(2));
				if (customPotion != null) { inv.setItem(2, new ItemStack(Material.AIR)); }
			}, 1L);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		if (event.getDestination().getType() != InventoryType.BREWING) { return; }

		TaskUtils.scheduleSyncDelayedTask(() -> {
			Inventory inv = event.getDestination();
			for (int i = 0; i < inv.getSize(); i++) {
				CustomPotion customPotion = CustomPotions.manager.getCustomPotion(inv.getItem(i));
				if (customPotion != null) { inv.setItem(i, customPotion.setPotionTag(inv.getItem(i))); }
			}
		}, 1L);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		ItemStack result = event.getInventory().getResult();
		if ((result == null) || (result.getType() != Material.TIPPED_ARROW)) { return; }

		ItemStack item = event.getInventory().getMatrix()[4];
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(item);
		if (customPotion != null) { event.getInventory().setResult(customPotion.getTippedArrow(false, 8)); }
	}
}