package me.serbinskis.smptweaks.custom.betterfurnaces.blocks;

import me.serbinskis.smptweaks.custom.betterfurnaces.BetterFurnaces;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomMarker;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CustomFurnace extends CustomBlock {
    private static final HashMap<String, Boolean> BLOCKS = new HashMap<>();
    private final int cook_time_total;

    public CustomFurnace(String id, int cook_time_total) {
        super(id, Material.FURNACE);
        this.cook_time_total = cook_time_total;
        this.setTickable(true);
    }

    @Override
    public ItemStack prepareCraft(PrepareItemCraftEvent event, World world, ItemStack result) {
        if (!BetterFurnaces.tweak.getGameRuleBoolean(world)) { return null; }
        return super.prepareCraft(event, world, result);
    }

    @Override
    public void tick(Block block, long tick) {
        Furnace furnace = (Furnace) block.getState();
        furnace.setCookTimeTotal(cook_time_total);
        furnace.update();

        String location = Utils.locationToString(block.getLocation());
        boolean isBurning = ((Furnace) block.getState()).getBurnTime() > 0;
        boolean previous = BLOCKS.computeIfAbsent(location, k -> !isBurning);
        if (previous == isBurning) { return; } //Don't tick if power is the same as previous
        BLOCKS.put(location, isBurning); //Update previous power to current

        CustomMarker marker = CustomMarker.getMarker(block);
        if (marker != null) { marker.updateTexture(block); }
    }

    @Override
    public int prepareTextureIndex(Block block) {
        return (((Furnace) block.getState()).getBurnTime() > 0) ? 1 : 0;
    }
}
