package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;

public interface RecipeHolder {

    void setRecipeUsed(@Nullable IRecipe<?> irecipe);

    @Nullable
    IRecipe<?> getRecipeUsed();

    default void awardUsedRecipes(EntityHuman entityhuman, List<ItemStack> list) {
        IRecipe<?> irecipe = this.getRecipeUsed();

        if (irecipe != null) {
            entityhuman.triggerRecipeCrafted(irecipe, list);
            if (!irecipe.isSpecial()) {
                entityhuman.awardRecipes(Collections.singleton(irecipe));
                this.setRecipeUsed((IRecipe) null);
            }
        }

    }

    default boolean setRecipeUsed(World world, EntityPlayer entityplayer, IRecipe<?> irecipe) {
        if (!irecipe.isSpecial() && world.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !entityplayer.getRecipeBook().contains(irecipe)) {
            return false;
        } else {
            this.setRecipeUsed(irecipe);
            return true;
        }
    }
}
