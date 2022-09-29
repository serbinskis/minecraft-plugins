package net.minecraft.world;

import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;

// CraftBukkit start
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class InventoryLargeChest implements IInventory {

    public final IInventory container1;
    public final IInventory container2;

    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();

    public List<ItemStack> getContents() {
        List<ItemStack> result = new ArrayList<ItemStack>(this.getContainerSize());
        for (int i = 0; i < this.getContainerSize(); i++) {
            result.add(this.getItem(i));
        }
        return result;
    }

    public void onOpen(CraftHumanEntity who) {
        this.container1.onOpen(who);
        this.container2.onOpen(who);
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        this.container1.onClose(who);
        this.container2.onClose(who);
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    public org.bukkit.inventory.InventoryHolder getOwner() {
        return null; // This method won't be called since CraftInventoryDoubleChest doesn't defer to here
    }

    public void setMaxStackSize(int size) {
        this.container1.setMaxStackSize(size);
        this.container2.setMaxStackSize(size);
    }

    @Override
    public Location getLocation() {
        return container1.getLocation(); // TODO: right?
    }
    // CraftBukkit end

    public InventoryLargeChest(IInventory iinventory, IInventory iinventory1) {
        this.container1 = iinventory;
        this.container2 = iinventory1;
    }

    @Override
    public int getContainerSize() {
        return this.container1.getContainerSize() + this.container2.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.container1.isEmpty() && this.container2.isEmpty();
    }

    public boolean contains(IInventory iinventory) {
        return this.container1 == iinventory || this.container2 == iinventory;
    }

    @Override
    public ItemStack getItem(int i) {
        return i >= this.container1.getContainerSize() ? this.container2.getItem(i - this.container1.getContainerSize()) : this.container1.getItem(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return i >= this.container1.getContainerSize() ? this.container2.removeItem(i - this.container1.getContainerSize(), j) : this.container1.removeItem(i, j);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return i >= this.container1.getContainerSize() ? this.container2.removeItemNoUpdate(i - this.container1.getContainerSize()) : this.container1.removeItemNoUpdate(i);
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        if (i >= this.container1.getContainerSize()) {
            this.container2.setItem(i - this.container1.getContainerSize(), itemstack);
        } else {
            this.container1.setItem(i, itemstack);
        }

    }

    @Override
    public int getMaxStackSize() {
        return Math.min(this.container1.getMaxStackSize(), this.container2.getMaxStackSize()); // CraftBukkit - check both sides
    }

    @Override
    public void setChanged() {
        this.container1.setChanged();
        this.container2.setChanged();
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return this.container1.stillValid(entityhuman) && this.container2.stillValid(entityhuman);
    }

    @Override
    public void startOpen(EntityHuman entityhuman) {
        this.container1.startOpen(entityhuman);
        this.container2.startOpen(entityhuman);
    }

    @Override
    public void stopOpen(EntityHuman entityhuman) {
        this.container1.stopOpen(entityhuman);
        this.container2.stopOpen(entityhuman);
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemstack) {
        return i >= this.container1.getContainerSize() ? this.container2.canPlaceItem(i - this.container1.getContainerSize(), itemstack) : this.container1.canPlaceItem(i, itemstack);
    }

    @Override
    public void clearContent() {
        this.container1.clearContent();
        this.container2.clearContent();
    }
}
