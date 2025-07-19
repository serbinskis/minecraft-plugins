package me.serbinskis.smptweaks.library.customblocks.test;

import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class MovableBlock extends CustomBlock {
    public MovableBlock() {
        super("movable_block", Material.DIAMOND_BLOCK);
        this.setCustomName(Main.SYM_COLOR + "rMovable Block");
        this.setCustomTitle("Movable Block");
        this.setGlowing(ChatColor.BLACK);
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.shape("   ", " B ", "   ");
        recipe.setIngredient('B', Material.BARRIER);
        return recipe;
    }

    @Override
    public void remove(Block block, boolean intentional) {
        Utils.sendMessage("remove -> movable_block: (intentional: " + intentional + ") | " + block);
    }
}
