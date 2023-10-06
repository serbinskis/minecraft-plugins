package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class FurnaceRecipe extends RecipeCooking {

    public FurnaceRecipe(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i) {
        super(Recipes.SMELTING, s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.FURNACE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMELTING_RECIPE;
    }
}
