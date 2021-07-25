package com.willfp.ecoenchants.backpack;

import com.willfp.ecoenchants.enchantments.EcoEnchant;
import com.willfp.ecoenchants.enchantments.meta.EnchantmentType;
import com.willfp.ecoenchants.enchantments.util.EnchantChecks;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Backpack extends EcoEnchant {
    public Backpack() {
        super("backpack", EnchantmentType.SPECIAL);
    }

    @SuppressWarnings("deprecation")
	public Boolean hasEnchantmentBook(ItemStack item, String name) {
        for (Enchantment enchantment : ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().keySet()) {
        	if (enchantment.getName().equalsIgnoreCase(name)) {
        		return true;
        	}
        }

        return false;
    }

    @SuppressWarnings("deprecation")
	public Boolean hasEnchantmentShulker(ItemStack item, String name) {
    	for (Enchantment enchantment : item.getItemMeta().getEnchants().keySet()) {
        	if (enchantment.getName().equalsIgnoreCase(name)) {
        		return true;
        	}
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilPrepare(@NotNull final PrepareAnvilEvent event) {
    	AnvilInventory inv = event.getInventory();

    	//Get item and book
    	ItemStack item = inv.getContents()[0];
    	ItemStack book = inv.getContents()[1];

    	//Check if items are placed
    	if (book == null || item == null) {
    		return;
    	}

    	//Check for item type
    	if (book.getType() != Material.ENCHANTED_BOOK || !item.getType().toString().contains("SHULKER_BOX")) {
    		return;
    	}

    	//Check for amount (duped items)
    	if (book.getAmount() != 1 || item.getAmount() != 1) {
    		return;
    	}

    	//Check enchantment
        if (!hasEnchantmentBook(book, "BACKPACK")) {
        	return;
        }

        ItemStack newShulker = item.clone();
        newShulker.addUnsafeEnchantment(this, 1);
        ItemMeta meta = newShulker.getItemMeta();
        meta.setLore(Arrays.asList("Backpack"));

        newShulker.setItemMeta(meta);
        event.setResult(newShulker);
    	inv.setRepairCost(5);

    	if (inv.getRenameText().equals("")) {
    		return;
    	}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.getPlugin(), new Runnable() {
            public void run() {
            	inv.setItem(2, newShulker);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(@NotNull final InventoryClickEvent  event) {
    	Inventory inv = event.getWhoClicked().getOpenInventory().getTopInventory();

    	if (inv != null && inv.getType() == InventoryType.GRINDSTONE) {
        	//Get 1st and 2nd item
        	ItemStack i1 = inv.getContents()[0];
        	ItemStack i2 = inv.getContents()[1];
        	ItemStack shulker;

        	//Check if items are placed
        	if (i1 == null || !i1.getType().toString().contains("SHULKER_BOX")) &&
        	    (i2 == null || !i2.getType().toString().contains("SHULKER_BOX"))
        	   ) {
        		return;
        	}

        	//Check for item type
        	if (book.getType() != Material.ENCHANTED_BOOK || !item.getType().toString().contains("SHULKER_BOX")) {
        		return;
        	}
    	}
    }

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(@NotNull final BlockPlaceEvent event) {
        //Check if item has enchantment
        if (EnchantChecks.mainhand(event.getPlayer(), this) || EnchantChecks.offhand(event.getPlayer(), this)) {
        	event.setCancelled(true);
        }
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockDispense(@NotNull final BlockDispenseEvent event) {
    	//Disallow shulker place via dropper
        if (event.getItem().getType().toString().contains("SHULKER_BOX") && hasEnchantmentShulker(event.getItem(), "BACKPACK")) {
        	event.setCancelled(true);
        }
	}


	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(@NotNull final PlayerInteractEvent event) {
        //Check for disabled worlds
        if (this.getDisabledWorlds().contains(event.getPlayer().getWorld())) {
            return;
        }

        //Check if rigth clicked air
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
        	return;
        }

        //Check if item has enchantment
        if (!event.getItem().getType().toString().contains("SHULKER_BOX") || !hasEnchantmentShulker(event.getItem(), "BACKPACK")) {
        	return;
        }
 
        //Open backpack
        BackpackGUI backpackGUI = new BackpackGUI(event.getItem());
        event.getPlayer().openInventory(backpackGUI.getInventory());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(@NotNull final InventoryCloseEvent event) {
		Inventory inv = event.getInventory();

		if (inv != null && (inv.getHolder() instanceof BackpackGUI)) {
			((BackpackGUI) inv.getHolder()).saveInventory();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(@NotNull final PlayerDeathEvent event) {
		Inventory inv = event.getEntity().getOpenInventory().getTopInventory();

		if (inv != null && (inv.getHolder() instanceof BackpackGUI)) {
			((BackpackGUI) inv.getHolder()).saveInventory();
		}
	}
}
