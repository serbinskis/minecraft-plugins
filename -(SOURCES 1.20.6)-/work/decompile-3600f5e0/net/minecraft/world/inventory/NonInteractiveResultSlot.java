package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;

public class NonInteractiveResultSlot extends Slot {

    public NonInteractiveResultSlot(IInventory iinventory, int i, int j, int k) {
        super(iinventory, i, j, k);
    }

    @Override
    public void onQuickCraft(ItemStack itemstack, ItemStack itemstack1) {}

    @Override
    public boolean mayPickup(EntityHuman entityhuman) {
        return false;
    }

    @Override
    public Optional<ItemStack> tryRemove(int i, int j, EntityHuman entityhuman) {
        return Optional.empty();
    }

    @Override
    public ItemStack safeTake(int i, int j, EntityHuman entityhuman) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeInsert(ItemStack itemstack) {
        return itemstack;
    }

    @Override
    public ItemStack safeInsert(ItemStack itemstack, int i) {
        return this.safeInsert(itemstack);
    }

    @Override
    public boolean allowModification(EntityHuman entityhuman) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack itemstack) {
        return false;
    }

    @Override
    public ItemStack remove(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onTake(EntityHuman entityhuman, ItemStack itemstack) {}

    @Override
    public boolean isHighlightable() {
        return false;
    }

    @Override
    public boolean isFake() {
        return true;
    }
}
