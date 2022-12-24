package net.minecraft.world;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// CraftBukkit start
import net.minecraft.world.item.crafting.IRecipe;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
// CraftBukkit end

public interface IInventory extends Clearable {

    int LARGE_MAX_STACK_SIZE = 64;

    int getContainerSize();

    boolean isEmpty();

    ItemStack getItem(int i);

    ItemStack removeItem(int i, int j);

    ItemStack removeItemNoUpdate(int i);

    void setItem(int i, ItemStack itemstack);

    int getMaxStackSize(); // CraftBukkit

    void setChanged();

    boolean stillValid(EntityHuman entityhuman);

    default void startOpen(EntityHuman entityhuman) {}

    default void stopOpen(EntityHuman entityhuman) {}

    default boolean canPlaceItem(int i, ItemStack itemstack) {
        return true;
    }

    default int countItem(Item item) {
        int i = 0;

        for (int j = 0; j < this.getContainerSize(); ++j) {
            ItemStack itemstack = this.getItem(j);

            if (itemstack.getItem().equals(item)) {
                i += itemstack.getCount();
            }
        }

        return i;
    }

    default boolean hasAnyOf(Set<Item> set) {
        return this.hasAnyMatching((itemstack) -> {
            return !itemstack.isEmpty() && set.contains(itemstack.getItem());
        });
    }

    default boolean hasAnyMatching(Predicate<ItemStack> predicate) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemstack = this.getItem(i);

            if (predicate.test(itemstack)) {
                return true;
            }
        }

        return false;
    }

    // CraftBukkit start
    java.util.List<ItemStack> getContents();

    void onOpen(CraftHumanEntity who);

    void onClose(CraftHumanEntity who);

    java.util.List<org.bukkit.entity.HumanEntity> getViewers();

    org.bukkit.inventory.InventoryHolder getOwner();

    void setMaxStackSize(int size);

    org.bukkit.Location getLocation();

    default IRecipe getCurrentRecipe() {
        return null;
    }

    default void setCurrentRecipe(IRecipe recipe) {
    }

    int MAX_STACK = 64;
    // CraftBukkit end
}
