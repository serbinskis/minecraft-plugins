package net.minecraft.world;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;

// CraftBukkit start
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
// CraftBukkit end

public interface IInventory extends Clearable {

    float DEFAULT_DISTANCE_BUFFER = 4.0F;

    int getContainerSize();

    boolean isEmpty();

    ItemStack getItem(int i);

    ItemStack removeItem(int i, int j);

    ItemStack removeItemNoUpdate(int i);

    void setItem(int i, ItemStack itemstack);

    int getMaxStackSize(); // CraftBukkit

    default int getMaxStackSize(ItemStack itemstack) {
        return Math.min(this.getMaxStackSize(), itemstack.getMaxStackSize());
    }

    void setChanged();

    boolean stillValid(EntityHuman entityhuman);

    default void startOpen(EntityHuman entityhuman) {}

    default void stopOpen(EntityHuman entityhuman) {}

    default boolean canPlaceItem(int i, ItemStack itemstack) {
        return true;
    }

    default boolean canTakeItem(IInventory iinventory, int i, ItemStack itemstack) {
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

    static boolean stillValidBlockEntity(TileEntity tileentity, EntityHuman entityhuman) {
        return stillValidBlockEntity(tileentity, entityhuman, 4.0F);
    }

    static boolean stillValidBlockEntity(TileEntity tileentity, EntityHuman entityhuman, float f) {
        World world = tileentity.getLevel();
        BlockPosition blockposition = tileentity.getBlockPos();

        return world == null ? false : (world.getBlockEntity(blockposition) != tileentity ? false : entityhuman.canInteractWithBlock(blockposition, (double) f));
    }

    // CraftBukkit start
    java.util.List<ItemStack> getContents();

    void onOpen(CraftHumanEntity who);

    void onClose(CraftHumanEntity who);

    java.util.List<org.bukkit.entity.HumanEntity> getViewers();

    org.bukkit.inventory.InventoryHolder getOwner();

    void setMaxStackSize(int size);

    org.bukkit.Location getLocation();

    default RecipeHolder<?> getCurrentRecipe() {
        return null;
    }

    default void setCurrentRecipe(RecipeHolder<?> recipe) {
    }

    int MAX_STACK = 99;
    // CraftBukkit end
}
