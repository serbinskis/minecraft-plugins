package me.wobbychip.recallpotion.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

import me.wobbychip.recallpotion.BrewManager;
import me.wobbychip.recallpotion.Main;
import me.wobbychip.recallpotion.Utilities;

public class InventoryEvents implements Listener {
	@EventHandler(priority=EventPriority.MONITOR)
    public void onBrew(BrewEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getContents().getIngredient() == null) { return; }

		for (int i = 0; i < 3; i++) {
			ItemStack item = event.getContents().getItem(i);

			if (Utilities.isPotion(item) && (item.getItemMeta() != null) && (item.getItemMeta().getLocalizedName() != null)) {
				String name = item.getItemMeta().getLocalizedName();
				ItemStack ingredient = event.getContents().getIngredient();
				PotionData potionData = ((PotionMeta) item.getItemMeta()).getBasePotionData();
				//If item not potion this causes error ^ - fixed

				//Brewing potion
				if ((potionData.getType() == Main.potionBase) && (ingredient.getType() == Main.potionIngredient)) {
					switch (item.getType()) {
						case POTION: {
							event.getContents().setItem(i, Main.potionItem);
							break;
						}
						case SPLASH_POTION: {
							event.getContents().setItem(i, Main.splashPotionItem);
							break;
						}
						case LINGERING_POTION: {
							event.getContents().setItem(i, Main.lingeringPotionItem);
							break;
						}
						default: break;
					}
				}

				//Brewing splash potion
				if (name.equals(Main.potionItem.getItemMeta().getLocalizedName()) && (ingredient.getType() == Material.GUNPOWDER)) {
					event.getContents().setItem(i, Main.splashPotionItem);
				}

				//Brewing lingering potion
				if (name.equals(Main.splashPotionItem.getItemMeta().getLocalizedName()) && (ingredient.getType() == Material.DRAGON_BREATH)) {
					event.getContents().setItem(i, Main.lingeringPotionItem);
				}
			}
		}
    }

	//Move items
    @EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
    	if (event.isCancelled()) { return; }

    	//Check if inside brewing stand
        Inventory inv = event.getInventory();
        if (inv == null || inv.getType() != InventoryType.BREWING) { return; }

        //Check if clicked inside inventory
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) { return; }

        //Check if holder exists
        BrewingStand stand = ((BrewerInventory) inv).getHolder();
        if (stand == null) { return; }

        //Disable block state update to modify inventory
    	BrewManager brewManager = Main.brews.get(stand.getLocation());
    	if ((brewManager != null) && (
    			(clickedInv.getType() == InventoryType.BREWING) ||
    			(event.getClick() == ClickType.SHIFT_LEFT) ||
    			(event.getClick() == ClickType.SHIFT_RIGHT)
    	)) { brewManager.setDoUpdate(false); }

        //Handle inventory events
        switch (clickedInv.getType()) {
        	case PLAYER: {
        		onPlayerInevntoryClick(event); //Handle moving item with SHIFT clicking
        		break;
        	}
        	case BREWING: {
        		onBrewInevntoryClick(event); //Handle clicks in brewing inventory
        		break;
        	}
        	default: break;
        }

        //Add a timer if recipe is ok
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        public void run() {
	        	if ((brewManager != null) && !brewManager.getDoUpdate()) {
	        		brewManager.updateInventory(event.getView().getTopInventory());
	        		brewManager.setDoUpdate(true);
	        	}
	            Utilities.checkBrew(((BrewerInventory) inv).getHolder());
	        }
	    }, 1L);
    }

    //Drag items
	@EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event)  {
		if (event.isCancelled()) { return; }

    	//Check if inside brewing stand
        Inventory inv = event.getInventory();
        if (inv == null || inv.getType() != InventoryType.BREWING) { return; }

        //Check if holder exists
        BrewingStand stand = ((BrewerInventory) inv).getHolder();
        if (stand == null) { return; }

        //Disable block state update to modify inventory
    	BrewManager brewManager = Main.brews.get(stand.getLocation());
    	if (brewManager != null) { brewManager.setDoUpdate(false); }

    	//Add a timer if recipe is ok
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        public void run() {
	        	if ((brewManager != null) && !brewManager.getDoUpdate()) {
	        		brewManager.updateInventory(inv);
	        		brewManager.setDoUpdate(true);
	        	}
	            Utilities.checkBrew(((BrewerInventory) event.getInventory()).getHolder());
	        }
	    }, 1L);
	}

    //Fuel
	@EventHandler(priority=EventPriority.MONITOR)
    public void onBrewingStandFuel(BrewingStandFuelEvent event)  {
		if (event.isCancelled() || (event.getBlock() == null) || (event.getBlock().getState() == null)) { return; }

    	//Add a timer if recipe is ok
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        public void run() {
	            Utilities.checkBrew((BrewingStand) event.getBlock().getState());
	        }
	    }, 1L);
	}

    public void onPlayerInevntoryClick(InventoryClickEvent event) {
        //Get clicked item
        ItemStack clickedItem = event.getCurrentItem();
        if ((clickedItem == null) || (clickedItem.getType() != Main.potionIngredient)) { return; }
    	if ((event.getClick() != ClickType.SHIFT_LEFT) && (event.getClick() != ClickType.SHIFT_RIGHT)) { return; }
    	event.setCancelled(true);

    	//Get brewing ingredient
    	ItemStack ingredient = event.getInventory().getItem(3);

		//If empty just move item
		if (Utilities.isEmpty(ingredient)) {
			event.getInventory().setItem(3, clickedItem);
			clickedItem.setAmount(0);
			return;
		}

		//Compare if same
		if (!Utilities.isEquals(ingredient, clickedItem)) { return; }
		int howMuchAdd = (ingredient.getMaxStackSize() - ingredient.getAmount());

		if (howMuchAdd > clickedItem.getAmount()) {
			ingredient.setAmount(ingredient.getAmount() + clickedItem.getAmount());
			clickedItem.setAmount(0);
		} else {
			clickedItem.setAmount(clickedItem.getAmount() - howMuchAdd);
			ingredient.setAmount(ingredient.getMaxStackSize());
		}
	}

	public void onBrewInevntoryClick(InventoryClickEvent event) {
    	if (event.getSlot() != 3) { return; }
    	ItemStack ingredient = event.getInventory().getItem(3);
        ItemStack cursor = event.getCursor();

    	if (event.getClick() == ClickType.LEFT) {
    		if (Utilities.isEmpty(ingredient) && !Utilities.isEmpty(cursor)) {
    			//Brew is empty and cursor is not - Put item in brew
    			if (cursor.getType() != Main.potionIngredient) { return; } //Let the minecraft handle rest
    			event.getInventory().setItem(3, event.getCursor().clone());
    			event.getCursor().setAmount(0);
    			event.setCancelled(true);
    		} else if (!Utilities.isEmpty(ingredient) && !Utilities.isEmpty(cursor) && !Utilities.isEquals(ingredient, cursor)) {
				//Items are not same, just swap them
    			if ((ingredient.getType() != Main.potionIngredient) && (cursor.getType() != Main.potionIngredient)) { return; } //Let the minecraft handle rest
				ItemStack clone = ingredient.clone();
				event.getInventory().setItem(3, event.getCursor().clone());
				event.getWhoClicked().setItemOnCursor(clone);
				event.setCancelled(true);
    		} else if (!Utilities.isEmpty(ingredient) && !Utilities.isEmpty(cursor) && Utilities.isEquals(ingredient, cursor)) {
    			//Items are same combine them in brew
    			if (ingredient.getType() != Main.potionIngredient) { return; } //Let the minecraft handle rest
    			int howMuchAdd = (ingredient.getMaxStackSize() - ingredient.getAmount());

    			if (howMuchAdd > cursor.getAmount()) {
    				ingredient.setAmount(ingredient.getAmount() + cursor.getAmount());
    				cursor.setAmount(0);
    			} else {
    				cursor.setAmount(cursor.getAmount() - howMuchAdd);
    				ingredient.setAmount(ingredient.getMaxStackSize());
    			}
    			event.setCancelled(true);
    		} else {
    			//Clicking with empty cursor on empty slot
    			//Cursor is empty and brew is not - Put item in cursor
    			return; //Let the minecraft handle rest
    		}
    	}

    	if (event.getClick() == ClickType.RIGHT) {
    		if (!Utilities.isEmpty(ingredient) && !Utilities.isEmpty(cursor) && Utilities.isEquals(ingredient, cursor)) {
    			//Items are same then add +1 to ingredient
    			if (ingredient.getAmount() == ingredient.getMaxStackSize()) { return; }
    			ingredient.setAmount(ingredient.getAmount()+1);
    			cursor.setAmount(cursor.getAmount()-1);
    			event.setCancelled(true);
    		} else if (Utilities.isEmpty(ingredient) && !Utilities.isEmpty(cursor)) {
    			//If brewing stand is empty put one item inside
    			if (cursor.getType() != Main.potionIngredient) { return; } //Let the minecraft handle rest
    			event.getInventory().setItem(3, event.getCursor().clone());
    			event.getInventory().getItem(3).setAmount(1);
    			cursor.setAmount(cursor.getAmount()-1);
    			event.setCancelled(true);
    		} else if (!Utilities.isEmpty(ingredient) && !Utilities.isEmpty(cursor) && !Utilities.isEquals(ingredient, cursor)) {
    			//Items are not same, just swap them
    			if ((ingredient.getType() != Main.potionIngredient) && (cursor.getType() != Main.potionIngredient)) { return; } //Let the minecraft handle rest
				ItemStack clone = ingredient.clone();
				event.getInventory().setItem(3, event.getCursor().clone());
				event.getWhoClicked().setItemOnCursor(clone);
				event.setCancelled(true);
    		} else {
    			//Let the minecraft handle rest
    			return;
    		}
    	}
	}
}