package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class RecipeCampfire extends RecipeCooking {

    public RecipeCampfire(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i) {
        super(Recipes.CAMPFIRE_COOKING, s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.CAMPFIRE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
    }
}
