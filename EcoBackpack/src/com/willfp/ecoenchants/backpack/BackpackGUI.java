package com.willfp.ecoenchants.backpack;

import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class BackpackGUI implements InventoryHolder {
	private Inventory inv;
	private ItemStack item;

	public BackpackGUI(ItemStack item) {
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta blockMeta = (BlockStateMeta) item.getItemMeta();
            if (blockMeta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulker = (ShulkerBox) blockMeta.getBlockState();

                if (item.getItemMeta().hasDisplayName()) {
                	inv = Bukkit.createInventory(this, InventoryType.SHULKER_BOX, item.getItemMeta().getDisplayName());
                } else {
                	inv = Bukkit.createInventory(this, InventoryType.SHULKER_BOX, "Shulker Box");
                }

                inv.setContents(shulker.getInventory().getContents());
                this.item = item;
            }
        }
	}

	public void saveInventory() {
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta blockMeta = (BlockStateMeta) item.getItemMeta();
            if (blockMeta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulker = (ShulkerBox) blockMeta.getBlockState();
                shulker.getInventory().setContents(inv.getContents());
                blockMeta.setBlockState(shulker);
                item.setItemMeta(blockMeta);
                Bukkit.getConsoleSender().sendMessage("Saved inv");
            }
        }
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}
}
