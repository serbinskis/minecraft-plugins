package me.wobbychip.smptweaks.custom.custombreaking.breaking;

import me.wobbychip.smptweaks.custom.custombreaking.CustomBreaking;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class CustomBlock {
    public Material type;
    public float destroyTime;

    public CustomBlock(Material type, float destroyTime) {
        this.type = type;
        this.destroyTime = destroyTime;
    }

    public Material getType() {
        return type;
    }

    public float getDestroyTime() {
        return destroyTime;
    }

    public boolean isEnabled() {
        ConfigurationSection blocks = CustomBreaking.config.getConfig().getConfigurationSection("blocks");
        String section = this.getType().toString().toLowerCase();

        if (!blocks.isConfigurationSection(section)) {
            blocks.createSection(section);
            blocks.getConfigurationSection(section).set("enabled", true);
            CustomBreaking.config.save();
        }

        return blocks.getConfigurationSection(section).getBoolean("enabled");
    }

    public boolean shouldDropItem(Block block, Player player) { return true; }
    public boolean shouldDropExp(Block block, Player player) { return true; }
    public ItemStack getDropItem(Block block, Player player) { return new ItemStack(Material.AIR); }
    public boolean hasCorrectToolForDrops(Block block, Player player) { return true; }
    public void onBlockBreakEvent(BlockBreakEvent event) {}
    public void onBlockPlaceEvent(BlockPlaceEvent event) {}
    public boolean isCustomBlock(Block block) {
        return type.equals(block.getType()) && isEnabled();
    }
}
