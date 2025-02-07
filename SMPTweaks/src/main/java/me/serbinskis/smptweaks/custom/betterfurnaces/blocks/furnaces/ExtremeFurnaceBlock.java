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

public class ExtremeFurnaceBlock extends CustomFurnace {
    public ExtremeFurnaceBlock() {
        super("extreme_furnace_block", 1);
        this.setCustomName(Main.SYM_COLOR + "rExtreme Furnace");
        this.setCustomTitle("Extreme Furnace");
        this.setTexture("extreme_furnace_block.png");
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.MISC);
        recipe.shape("AEA", "SFS", "AEA");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(new NetheriteFurnaceBlock().getDropItem(0)));
        return recipe;
    }
}
