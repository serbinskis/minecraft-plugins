package me.wobbychip.smptweaks.custom.custombreaking.custom;

import me.wobbychip.smptweaks.custom.custombreaking.breaking.CustomBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomBedrock extends CustomBlock {
    public CustomBedrock() {
        super(Material.BEDROCK, 17280f);
    }

    @Override
    public boolean hasCorrectToolForDrops(Block block, Player player) {
        return List.of(Material.NETHERITE_PICKAXE, Material.DIAMOND_PICKAXE).contains(player.getInventory().getItemInMainHand().getType());
    }

    @Override
    public ItemStack getDropItem(Block block, Player player) {
        return new ItemStack(Material.BEDROCK);
    }
}
