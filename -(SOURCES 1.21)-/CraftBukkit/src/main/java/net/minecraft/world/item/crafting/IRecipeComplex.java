package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

// CraftBukkit start
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
// CraftBukkit end

public abstract class IRecipeComplex implements RecipeCrafting {

    private final CraftingBookCategory category;

    public IRecipeComplex(CraftingBookCategory craftingbookcategory) {
        this.category = craftingbookcategory;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.a holderlookup_a) {
        return ItemStack.EMPTY;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    // CraftBukkit start
    @Override
    public Recipe toBukkitRecipe(NamespacedKey id) {
        return new org.bukkit.craftbukkit.inventory.CraftComplexRecipe(id, this);
    }
    // CraftBukkit end
}
