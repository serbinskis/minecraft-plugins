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

public class NetheriteFurnaceBlock extends CustomFurnace {
    public NetheriteFurnaceBlock() {
        super("netherite_furnace_block", 5);
        this.setCustomName(Main.SYM_COLOR + "rNetherite Furnace");
        this.setCustomTitle("Netherite Furnace");
        this.setTexture("netherite_furnace_block.png");
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.MISC);
        recipe.shape("DDD", "NFN", "DDD");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(new EmeraldFurnaceBlock().getDropItem(0)));
        return recipe;
    }
}
