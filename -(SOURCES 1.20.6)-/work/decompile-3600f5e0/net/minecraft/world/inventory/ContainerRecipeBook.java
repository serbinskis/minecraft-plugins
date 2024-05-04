package net.minecraft.world.inventory;

import net.minecraft.recipebook.AutoRecipe;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class ContainerRecipeBook<C extends IInventory> extends Container {

    public ContainerRecipeBook(Containers<?> containers, int i) {
        super(containers, i);
    }

    public void handlePlacement(boolean flag, RecipeHolder<?> recipeholder, EntityPlayer entityplayer) {
        (new AutoRecipe<>(this)).recipeClicked(entityplayer, recipeholder, flag);
    }

    public abstract void fillCraftSlotsStackedContents(AutoRecipeStackManager autorecipestackmanager);

    public abstract void clearCraftingContent();

    public abstract boolean recipeMatches(RecipeHolder<? extends IRecipe<C>> recipeholder);

    public abstract int getResultSlotIndex();

    public abstract int getGridWidth();

    public abstract int getGridHeight();

    public abstract int getSize();

    public abstract RecipeBookType getRecipeBookType();

    public abstract boolean shouldMoveToInventory(int i);
}
