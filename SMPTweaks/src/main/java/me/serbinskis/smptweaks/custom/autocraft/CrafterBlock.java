package me.serbinskis.smptweaks.custom.autocraft;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

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
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.REDSTONE);
        recipe.shape("III", "IDI", "RCR");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('D', Material.DISPENSER);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        return recipe;
    }

    @Override
    public ItemStack prepareCraft(PrepareItemCraftEvent event, World world, ItemStack result) {
        if (!AutoCraft.tweak.getGameRuleBoolean(world)) { return null; }
        return super.prepareCraft(event, world, result);
    }

    @Override
    public boolean prepareDispense(Block block, HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense) {
        if (!AutoCraft.tweak.getGameRuleBoolean(block.getWorld())) { return false; }
        Crafters.handleCrafter(block).forEach(e -> dispense.put(e, Map.entry(new ItemStack(Material.AIR), -1)));
        return !dispense.isEmpty();
    }
}
