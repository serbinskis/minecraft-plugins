package net.minecraft.world.item.crafting;

public interface RecipeCrafting extends IRecipe<CraftingInput> {

    @Override
    default Recipes<?> getType() {
        return Recipes.CRAFTING;
    }

    CraftingBookCategory category();
}
