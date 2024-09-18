package me.serbinskis.smptweaks.custom.custompotions.events;

import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.BrewerInventory;
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
			if ((customPotion != null) && gameRule) { event.getResults().set(i, customPotion.setProperties(item, true)); }
		}

		//Because potion tag is lost after the event, we need to update it in the next tick
		TaskUtils.scheduleSyncDelayedTask(() -> {
			for (int i = 0; i < 3; i++) {
				ItemStack item = event.getContents().getItem(i);
				CustomPotion customPotion = CustomPotions.manager.getCustomPotion(item);
				if ((customPotion != null) && !gameRule) { event.getResults().set(i, customPotion.getDisabledPotion(item)); }
				if ((customPotion != null) && gameRule) { event.getContents().setItem(i, customPotion.setProperties(item, true)); }
			}
		}, 1L);
	}

	//Fix potion tag, cuz bukkit is trash and don't support custom potion tag
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		//When putting item inside brewing stand, convert it to nms potion
		if (event.getInventory() instanceof BrewerInventory) {
			CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getCurrentItem());
			if (customPotion != null) { event.setCurrentItem(customPotion.setPotionTag(event.getCurrentItem())); }
		}

		//When moving item from brewing stand to inventory, just convert it back to fake potion
		TaskUtils.scheduleSyncDelayedTask(() -> {
			Inventory inv = event.getView().getBottomInventory();
			for (int i = 0; i < inv.getSize(); i++) {
				if (CustomPotions.manager.getCustomPotion(inv.getItem(i)) == null) { continue; }
				inv.setItem(i, ReflectionUtils.setPotionTag(inv.getItem(i), CustomPotion.PLACEHOLDER_POTION));
			}
		}, 1L);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		//When putting item inside brewing stand, convert it to nms potion
		if (event.getInventory() instanceof BrewerInventory inv) {
			TaskUtils.scheduleSyncDelayedTask(() -> {
				for (int i = 0; i < inv.getSize(); i++) {
					CustomPotion customPotion = CustomPotions.manager.getCustomPotion(inv.getItem(i));
					if (customPotion != null) { inv.setItem(i, customPotion.setPotionTag(inv.getItem(i))); }
				}
			}, 1L);
		}

		//Check for every inventory, because InventoryMoveItemEvent doesn't work when taking item from source
		//When moving item from brewing stand to inventory, just convert it back to fake potion
		TaskUtils.scheduleSyncDelayedTask(() -> {
			Inventory inv = event.getView().getBottomInventory();
			for (int i = 0; i < inv.getSize(); i++) {
				if (CustomPotions.manager.getCustomPotion(inv.getItem(i)) == null) { continue; }
				inv.setItem(i, ReflectionUtils.setPotionTag(inv.getItem(i), CustomPotion.PLACEHOLDER_POTION));
			}
		}, 1L);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		//When moving item to brewing stand, convert it to nms potion
		if (event.getDestination() instanceof BrewerInventory inv) {
			TaskUtils.scheduleSyncDelayedTask(() -> {
				for (int i = 0; i < inv.getSize(); i++) {
					CustomPotion customPotion = CustomPotions.manager.getCustomPotion(inv.getItem(i));
					if (customPotion != null) { inv.setItem(i, customPotion.setPotionTag(inv.getItem(i))); }
				}
			}, 1L);
		}

		//InventoryMoveItemEvent doesn't work when taking item from source, so this only works when moving to, not from
		//When moving item from brewing stand to inventory, just convert it back to fake potion
		if (event.getDestination().getType() != InventoryType.BREWING) {
			TaskUtils.scheduleSyncDelayedTask(() -> {
				Inventory inv = event.getDestination();
				for (int i = 0; i < inv.getSize(); i++) {
					if (CustomPotions.manager.getCustomPotion(inv.getItem(i)) == null) { continue; }
					inv.setItem(i, ReflectionUtils.setPotionTag(inv.getItem(i), CustomPotion.PLACEHOLDER_POTION));
				}
			}, 1L);
		}
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