package net.minecraft.world.item.crafting;

import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;

// CraftBukkit start
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftStonecuttingRecipe;
import org.bukkit.inventory.Recipe;
// CraftBukkit end

public class RecipeStonecutting extends RecipeSingleItem {

    public RecipeStonecutting(String s, RecipeItemStack recipeitemstack, ItemStack itemstack) {
        super(Recipes.STONECUTTING, RecipeSerializer.STONECUTTER, s, recipeitemstack, itemstack);
    }

    @Override
    public boolean matches(IInventory iinventory, World world) {
        return this.ingredient.test(iinventory.getItem(0));
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.STONECUTTER);
    }

    // CraftBukkit start
    @Override
    public Recipe toBukkitRecipe(NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);

        CraftStonecuttingRecipe recipe = new CraftStonecuttingRecipe(id, result, CraftRecipe.toBukkit(this.ingredient));
        recipe.setGroup(this.group);

        return recipe;
    }
    // CraftBukkit end
}
