package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;

public class RecipeStonecutting extends RecipeSingleItem {

    public RecipeStonecutting(String s, RecipeItemStack recipeitemstack, ItemStack itemstack) {
        super(Recipes.STONECUTTING, RecipeSerializer.STONECUTTER, s, recipeitemstack, itemstack);
    }

    public boolean matches(SingleRecipeInput singlerecipeinput, World world) {
        return this.ingredient.test(singlerecipeinput.item());
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.STONECUTTER);
    }
}
