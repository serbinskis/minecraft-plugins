package me.serbinskis.smptweaks.custom.autotrade;

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

public class TraderBlock extends CustomBlock {
    public TraderBlock() {
        super("trader_block", Material.DISPENSER);
        this.setCustomName(Main.SYM_COLOR + "rAuto Trader");
        this.setCustomTitle("Auto Trader");
        this.setTexture("trader_block.png");
        this.setDispensable(Dispensable.CUSTOM);
        this.setComparable(Comparable.IGNORE);
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.REDSTONE);
        recipe.shape("EEE", "EDE", "RNR");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('D', Material.DISPENSER);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('N', Material.NETHER_STAR);
        return recipe;
    }

    @Override
    public ItemStack prepareCraft(PrepareItemCraftEvent event, World world, ItemStack result) {
        if (!AutoTrade.tweak.getGameRuleBoolean(world)) { return null; }
        return super.prepareCraft(event, world, result);
    }

    @Override
    public boolean prepareDispense(Block block, HashMap<ItemStack, Map.Entry<ItemStack, Integer>> dispense) {
        if (!AutoTrade.tweak.getGameRuleBoolean(block.getWorld())) { return false; }
        Traders.handleTrader(block).forEach(e -> dispense.put(e, Map.entry(new ItemStack(Material.AIR), -1)));
        return !dispense.isEmpty();
    }

    @Override
    public void remove(Block block, boolean intentional) {
        if (intentional) { Villagers.releaseXp(block, block.getLocation().clone().add(0.5, 0.5, 0.5)); }
    }

    /*@Override
    public void tick(Block block, long tick) {
        CrafterInventory inventory = (CrafterInventory) ((Container) block.getState()).getInventory();
        CraftInventoryCrafter inventory1 = (CraftInventoryCrafter) inventory;
        inventory1.getResultInventory().setItem(0, Objects.requireNonNull(ReflectionUtils.asNMSCopy(new ItemStack(Material.DIAMOND))));
        CrafterBlock;
    }*/
}
