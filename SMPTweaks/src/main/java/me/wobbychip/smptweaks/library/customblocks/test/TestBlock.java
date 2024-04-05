package me.wobbychip.smptweaks.library.customblocks.test;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestBlock extends CustomBlock {
    public TestBlock() {
        super("custom_block", Material.DISPENSER);
        this.setCustomModel(1000110000, 1000120000);
        this.setCustomName(Main.SYM_COLOR + "rCustom Block");
        this.setCustomTitle("Custom Block");
        this.setDispensable(Dispensable.CUSTOM);
        this.setComparable(Comparable.DISABLE);
        this.setGlowing(ChatColor.WHITE);
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.shape("AAA", "ABA", "AAA");
        recipe.setIngredient('A', Material.AIR);
        recipe.setIngredient('B', Material.BEDROCK);
        return recipe;
    }

    @Override
    public int preparePower(Block block) {
        if (!(block.getState() instanceof Container)) { return 0; }
        return (int) Arrays.stream(((Container) block.getState()).getInventory().getContents()).filter(e -> (e != null && e.getType() != Material.AIR)).count();
    }

    @Override
    public boolean prepareDispense(Block block, HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense) {
        if (!(block.getState() instanceof Container)) { return false; }

        ItemStack drop = Arrays.stream(((Container) block.getState()).getInventory().getContents()).filter(e -> (e != null && e.getType() != Material.AIR)).findFirst().orElse(null);
        if (drop == null) { return false; } else { drop = drop.clone(); }
        //drop.setAmount(1);

        //dispense.put(drop, Map.entry(new ItemStack(Material.AIR), -1));
        dispense.put(drop, Map.entry(drop, -1));
        return true;
    }
}
