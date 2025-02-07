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

public class DiamondFurnaceBlock extends CustomFurnace {
    public DiamondFurnaceBlock() {
        super("diamond_furnace_block", 20);
        this.setCustomName(Main.SYM_COLOR + "rDiamond Furnace");
        this.setCustomTitle("Diamond Furnace");
        this.setTexture("diamond_furnace_block.png");
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.MISC);
        recipe.shape("DDD", "DFD", "DDD");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(new GoldenFurnaceBlock().getDropItem(0)));
        return recipe;
    }
}
