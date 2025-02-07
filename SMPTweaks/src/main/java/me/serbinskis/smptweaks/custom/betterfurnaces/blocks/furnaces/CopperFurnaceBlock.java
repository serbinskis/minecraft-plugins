package me.serbinskis.smptweaks.custom.betterfurnaces.blocks.furnaces;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.betterfurnaces.blocks.CustomFurnace;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class CopperFurnaceBlock extends CustomFurnace {
    public CopperFurnaceBlock() {
        super("copper_furnace_block", 60);
        this.setCustomName(Main.SYM_COLOR + "rCopper Furnace");
        this.setCustomTitle("Copper Furnace");
        this.setTexture("copper_furnace_block.png");
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.MISC);
        recipe.shape("CCC", "CFC", "CCC");
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(new IronFurnaceBlock().getDropItem(0)));
        return recipe;
    }
}
