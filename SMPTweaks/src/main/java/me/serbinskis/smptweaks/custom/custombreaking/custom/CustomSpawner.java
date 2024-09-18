package me.serbinskis.smptweaks.custom.custombreaking.custom;

import me.serbinskis.smptweaks.custom.custombreaking.breaking.CustomBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.List;

public class CustomSpawner extends CustomBlock {
    public CustomSpawner() {
        super(Material.SPAWNER, -1f);
    }

    @Override
    public boolean hasCorrectToolForDrops(Block block, Player player) {
        return List.of(Material.IRON_PICKAXE, Material.NETHERITE_PICKAXE, Material.DIAMOND_PICKAXE).contains(player.getInventory().getItemInMainHand().getType());
    }

    @Override
    public boolean shouldDropItem(Block block, Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!hasCorrectToolForDrops(block, player)) { return false; }
        return item.getEnchantments().containsKey(Enchantment.SILK_TOUCH);
    }

    @Override
    public boolean shouldDropExp(Block block, Player player) {
        return false;
    }

    @Override
    public ItemStack getDropItem(Block block, Player player) {
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
        BlockStateMeta itemMeta = (BlockStateMeta) spawnerItem.getItemMeta();

        itemMeta.setBlockState(block.getState());
        itemMeta.addItemFlags();
        spawnerItem.setItemMeta(itemMeta);
        return spawnerItem;
    }

    @Override
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        ItemStack spawnerItem = event.getItemInHand();
        BlockStateMeta itemMeta = (BlockStateMeta) spawnerItem.getItemMeta();
        CreatureSpawner spawner = (CreatureSpawner) itemMeta.getBlockState();

        CreatureSpawner blockSate = (CreatureSpawner) event.getBlockPlaced().getState();
        blockSate.setDelay(spawner.getDelay());
        blockSate.setMaxNearbyEntities(spawner.getMaxNearbyEntities());
        blockSate.setMaxSpawnDelay(spawner.getMaxSpawnDelay());
        blockSate.setMinSpawnDelay(spawner.getMinSpawnDelay());
        blockSate.setRequiredPlayerRange(spawner.getRequiredPlayerRange());
        blockSate.setSpawnCount(spawner.getSpawnCount());
        blockSate.setSpawnedType(spawner.getSpawnedType());
        blockSate.setSpawnRange(spawner.getSpawnRange());
        blockSate.setBlockData(spawner.getBlockData());
        blockSate.update();
    }
}
