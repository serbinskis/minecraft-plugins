package me.serbinskis.smptweaks.custom.chunkloader.block;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.chunkloader.ChunkLoader;
import me.serbinskis.smptweaks.custom.chunkloader.loaders.Border;
import me.serbinskis.smptweaks.custom.chunkloader.loaders.Chunks;
import me.serbinskis.smptweaks.custom.chunkloader.loaders.Fakes;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import me.serbinskis.smptweaks.utils.ServerUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaderBlock extends CustomBlock {
    public static LoaderBlock LOADER_BLOCK;
    private static final HashMap<String, Boolean> BLOCKS = new HashMap<>();
    private static boolean busy = false;

    public LoaderBlock() {
        super("loader_block", Material.LODESTONE);
        this.setTexture("loader_block.png");
        this.setCustomName(Main.SYM_COLOR + "rChunk Loader");
        this.setTickable(true);
        LoaderBlock.LOADER_BLOCK = this;
    }

    @Override
    public Recipe prepareRecipe(NamespacedKey key, ItemStack itemStack) {
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.setCategory(CraftingBookCategory.REDSTONE);
        recipe.shape("GGG", "GLG", "RNR");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('L', Material.LODESTONE);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('N', Material.NETHER_STAR);
        return recipe;
    }

    @Override
    public ItemStack prepareCraft(PrepareItemCraftEvent event, World world, ItemStack result) {
        if (!ChunkLoader.tweak.getGameRuleBoolean(world)) { return null; }
        return super.prepareCraft(event, world, result);
    }

    @Override
    public ChatColor prepareGlowingColor(Block block) {
        boolean isPowered = isPowered(block) && ChunkLoader.tweak.getGameRuleBoolean(block.getWorld());
        return ChunkLoader.highlighting ? (isPowered ? ChatColor.GREEN : ChatColor.RED) : ChatColor.RESET;
    }

    @Override
    public void create(Block block, boolean new_block) {
        if (new_block) { block.getWorld().playSound(block.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1); }
        busy = true;
        tick(block, ServerUtils.getTick()); //Prevent repeating sound when placing block
        busy = false;
        saveAll();
    }

    @Override
    public void remove(Block block, boolean intentional) {
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
        BLOCKS.remove(Utils.locationToString(block.getLocation()));
        Fakes.setEnabled(false, block);
        Chunks.markChunks(block.getLocation(), ChunkLoader.viewDistance, false);
        Border.remove(block);
        saveAll();
    }

    @Override
    public void tick(Block block, long tick) {
        String location = Utils.locationToString(block.getLocation());
        boolean isPowered = isPowered(block) && ChunkLoader.tweak.getGameRuleBoolean(block.getWorld());
        boolean previous = BLOCKS.computeIfAbsent(location, k -> !isPowered);

        if (previous == isPowered) { return; } //Don't tick if power is the same as previous
        BLOCKS.put(location, isPowered); //Update previous power to current

        if (!busy) { block.getWorld().playSound(block.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1); }
        setGlowing(block, (ChunkLoader.highlighting ? (isPowered ? ChatColor.GREEN : ChatColor.RED) : ChatColor.RESET));
        Chunks.markChunks(block.getLocation(), ChunkLoader.viewDistance, isPowered);
        Fakes.setEnabled(isPowered, block);
    }

    public static void saveAll() {
        List<String> loaders = BLOCKS.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
        ChunkLoader.tweak.getConfig(1).getConfig().set("chunkloaders", loaders);
        ChunkLoader.tweak.getConfig(1).save();
    }
}
