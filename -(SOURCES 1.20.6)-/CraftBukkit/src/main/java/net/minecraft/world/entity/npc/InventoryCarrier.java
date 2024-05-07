package net.minecraft.world.entity.npc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;

// CraftBukkit start
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public interface InventoryCarrier {

    String TAG_INVENTORY = "Inventory";

    InventorySubcontainer getInventory();

    static void pickUpItem(EntityInsentient entityinsentient, InventoryCarrier inventorycarrier, EntityItem entityitem) {
        ItemStack itemstack = entityitem.getItem();

        if (entityinsentient.wantsToPickUp(itemstack)) {
            InventorySubcontainer inventorysubcontainer = inventorycarrier.getInventory();
            boolean flag = inventorysubcontainer.canAddItem(itemstack);

            if (!flag) {
                return;
            }

            // CraftBukkit start
            ItemStack remaining = new InventorySubcontainer(inventorysubcontainer).addItem(itemstack);
            if (org.bukkit.craftbukkit.event.CraftEventFactory.callEntityPickupItemEvent(entityinsentient, entityitem, remaining.getCount(), false).isCancelled()) {
                return;
            }
            // CraftBukkit end

            entityinsentient.onItemPickup(entityitem);
            int i = itemstack.getCount();
            ItemStack itemstack1 = inventorysubcontainer.addItem(itemstack);

            entityinsentient.take(entityitem, i - itemstack1.getCount());
            if (itemstack1.isEmpty()) {
                entityitem.discard(EntityRemoveEvent.Cause.PICKUP); // CraftBukkit - add Bukkit remove cause
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }

    }

    default void readInventoryFromTag(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        if (nbttagcompound.contains("Inventory", 9)) {
            this.getInventory().fromTag(nbttagcompound.getList("Inventory", 10), holderlookup_a);
        }

    }

    default void writeInventoryToTag(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        nbttagcompound.put("Inventory", this.getInventory().createTag(holderlookup_a));
    }
}
