package me.wobbychip.smptweaks.custom.silktouchspawners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType() != Material.SPAWNER) { return; }
		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) { return; }
		if (!event.isDropItems()) { return; }

		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
		if (!SilkTouchSpawners.correctTools.contains(item.getType())) { return; }
		if (!Utils.containsEnchantment(item, SilkTouchSpawners.silks)) { return; }

		CreatureSpawner blockState = (CreatureSpawner) event.getBlock().getState();
		ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
		BlockStateMeta itemMeta = (BlockStateMeta) spawnerItem.getItemMeta();

		itemMeta.setBlockState(blockState);
		itemMeta.addItemFlags();
		spawnerItem.setItemMeta(itemMeta);

		event.setExpToDrop(0);
		Utils.dropBlockItem(event.getBlock(), event.getPlayer(), spawnerItem);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (event.getBlockPlaced().getType() != Material.SPAWNER) { return; }

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
