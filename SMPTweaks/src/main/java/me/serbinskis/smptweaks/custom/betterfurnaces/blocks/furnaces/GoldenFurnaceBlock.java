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

public class GoldenFurnaceBlock extends CustomFurnace {
    public GoldenFurnaceBlock() {
        super("golden_furnace_block", 40);
        this.setCustomName(Main.SYM_COLOR + "rGolden Furnace");
        this.setCustomTitle("Golden Furnace");
        this.setTexture("golden_furnace_block.png");
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.MISC);
        recipe.shape("GGG", "GFG", "GGG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(new CopperFurnaceBlock().getDropItem(0)));
        return recipe;
    }
}
