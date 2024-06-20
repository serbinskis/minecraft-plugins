package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

// CraftBukkit start
import net.minecraft.world.item.crafting.RecipeHolder;
// CraftBukkit end

public interface InventoryCrafting extends IInventory, AutoRecipeOutput {

    int getWidth();

    int getHeight();

    List<ItemStack> getItems();

    // CraftBukkit start
    default RecipeHolder<?> getCurrentRecipe() {
        return null;
    }

    default void setCurrentRecipe(RecipeHolder<?> recipe) {
    }
    // CraftBukkit end

    default CraftingInput asCraftInput() {
        return this.asPositionedCraftInput().input();
    }

    default CraftingInput.a asPositionedCraftInput() {
        return CraftingInput.ofPositioned(this.getWidth(), this.getHeight(), this.getItems());
    }
}
