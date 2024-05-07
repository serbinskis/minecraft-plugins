package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;

public interface InventoryCrafting extends IInventory, AutoRecipeOutput {

    int getWidth();

    int getHeight();

    List<ItemStack> getItems();
}
