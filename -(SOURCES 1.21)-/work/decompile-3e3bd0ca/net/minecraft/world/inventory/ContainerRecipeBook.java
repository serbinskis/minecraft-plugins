package net.minecraft.world.inventory;

import net.minecraft.recipebook.AutoRecipe;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public abstract class ContainerRecipeBook<I extends RecipeInput, R extends IRecipe<I>> extends Container {

    public ContainerRecipeBook(Containers<?> containers, int i) {
        super(containers, i);
    }

    public void handlePlacement(boolean flag, RecipeHolder<?> recipeholder, EntityPlayer entityplayer) {
        RecipeHolder<R> recipeholder1 = recipeholder;

        this.beginPlacingRecipe();

        try {
            (new AutoRecipe<>(this)).recipeClicked(entityplayer, recipeholder1, flag);
        } finally {
            this.finishPlacingRecipe(recipeholder);
        }

    }

    protected void beginPlacingRecipe() {}

    protected void finishPlacingRecipe(RecipeHolder<R> recipeholder) {}

    public abstract void fillCraftSlotsStackedContents(AutoRecipeStackManager autorecipestackmanager);

    public abstract void clearCraftingContent();

    public abstract boolean recipeMatches(RecipeHolder<R> recipeholder);

    public abstract int getResultSlotIndex();

    public abstract int getGridWidth();

    public abstract int getGridHeight();

    public abstract int getSize();

    public abstract RecipeBookType getRecipeBookType();

    public abstract boolean shouldMoveToInventory(int i);
}
