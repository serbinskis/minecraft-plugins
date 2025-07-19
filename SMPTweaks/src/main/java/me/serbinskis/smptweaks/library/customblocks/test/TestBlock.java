package me.serbinskis.smptweaks.library.customblocks.test;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestBlock extends CustomBlock {
    public TestBlock() {
        super("custom_block", Material.DISPENSER);
        this.setTexture("crafter_block.png");
        this.setCustomName(Main.SYM_COLOR + "rCustom Block");
        this.setCustomTitle("Custom Block");
        this.setDispensable(Dispensable.CUSTOM);
        this.setGlowing(ChatColor.WHITE);
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.shape("   ", " B ", "   ");
        recipe.setIngredient('B', Material.BEDROCK);
        return recipe;
    }

    @Override
    public boolean prepareDispense(Block block, Inventory inventory, HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense) {
        if (!(block.getState() instanceof Container)) { return false; }
        ItemStack drop = Arrays.stream(inventory.getContents()).filter(Objects::nonNull).filter(e -> !Material.AIR.equals(e.getType())).findFirst().orElse(null);
        dispense.put(drop, Map.entry(drop, -1));
        return true;
    }

    @Override
    public void remove(Block block, boolean intentional) {
        Utils.sendMessage("remove -> custom_block: (intentional: " + intentional + ") | " + block);
    }
}
