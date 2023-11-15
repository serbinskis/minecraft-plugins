package me.wobbychip.smptweaks.library.customblocks.test;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class TestBlock extends CustomBlock {
    public TestBlock() {
        super("custom_block", Material.DROPPER);
        this.setCustomMaterial(Material.PISTON);
        this.setCustomTitle("Custom Block");
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key) {
        ShapedRecipe recipe = new ShapedRecipe(key, getDropItem());
        recipe.shape("AAA", "ABA", "AAA");
        recipe.setIngredient('A', Material.AIR);
        recipe.setIngredient('B', Material.BEDROCK);
        return recipe;
    }

    @Override
    public ItemStack prepareDropItem() {
        ItemStack item = new ItemStack(getBlockBase());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Main.sym_color + "dCustom Block");
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public int preparePower(Block block) {
        if (!(block.getState() instanceof Container)) { return 0; }
        return (int) Arrays.stream(((Container) block.getState()).getInventory().getContents()).filter(e -> (e != null && e.getType() != Material.AIR)).count();
    }
}
