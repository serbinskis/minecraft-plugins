package me.wobbychip.smptweaks.custom.autocraft;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Map;

public class CrafterBlock extends CustomBlock {
    public CrafterBlock() {
        super("crafter_block", Material.DISPENSER);
        this.setCustomModel(1000110000, 1000120000);
        this.setCustomName(Main.SYM_COLOR + "rAuto Crafter");
        this.setCustomTitle("Auto Crafter");
        this.setDispensable(Dispensable.CUSTOM);
        this.setComparable(Comparable.IGNORE);
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, getDropItem(false));
        recipe.shape("III", "IDI", "RCR");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('D', Material.DISPENSER);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        return recipe;
    }

    @Override
    public boolean prepareDispense(Block block, HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense) {
        if (!AutoCraft.tweak.getGameRuleBoolean((block.getWorld()))) { return false; }
        Crafters.handleCrafter(block).forEach(e -> dispense.put(e, Map.entry(e, -1)));
        return !dispense.isEmpty();
    }
}
