package net.minecraft.world.ticks;

import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;

public interface ContainerSingleItem extends IInventory {

    ItemStack getTheItem();

    default ItemStack splitTheItem(int i) {
        return this.getTheItem().split(i);
    }

    void setTheItem(ItemStack itemstack);

    default ItemStack removeTheItem() {
        return this.splitTheItem(this.getMaxStackSize());
    }

    @Override
    default int getContainerSize() {
        return 1;
    }

    @Override
    default boolean isEmpty() {
        return this.getTheItem().isEmpty();
    }

    @Override
    default void clearContent() {
        this.removeTheItem();
    }

    @Override
    default ItemStack removeItemNoUpdate(int i) {
        return this.removeItem(i, this.getMaxStackSize());
    }

    @Override
    default ItemStack getItem(int i) {
        return i == 0 ? this.getTheItem() : ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeItem(int i, int j) {
        return i != 0 ? ItemStack.EMPTY : this.splitTheItem(j);
    }

    @Override
    default void setItem(int i, ItemStack itemstack) {
        if (i == 0) {
            this.setTheItem(itemstack);
        }

    }

    public interface a extends ContainerSingleItem {

        TileEntity getContainerBlockEntity();

        @Override
        default boolean stillValid(EntityHuman entityhuman) {
            return IInventory.stillValidBlockEntity(this.getContainerBlockEntity(), entityhuman);
        }
    }
}
