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

public class EmeraldFurnaceBlock extends CustomFurnace {
    public EmeraldFurnaceBlock() {
        super("emerald_furnace_block", 10);
        this.setCustomName(Main.SYM_COLOR + "rEmerald Furnace");
        this.setCustomTitle("Emerald Furnace");
        this.setTexture("emerald_furnace_block.png");
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.MISC);
        recipe.shape("EEE", "EFE", "EEE");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(new DiamondFurnaceBlock().getDropItem(0)));
        return recipe;
    }
}
