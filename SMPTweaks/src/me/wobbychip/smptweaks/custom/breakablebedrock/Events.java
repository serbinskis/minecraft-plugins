package me.wobbychip.smptweaks.custom.breakablebedrock;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamageEvent(BlockDamageEvent event) {
		if (event.getBlock().getType() != Material.BEDROCK) { return; }
		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) { return; }
		if (BreakableBedrock.destroyTime < 0) { return; }
		BedrockBreaker.addPlayer(event.getPlayer(), event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamageAbortEvent(BlockDamageAbortEvent event) {
		if (event.getBlock().getType() != Material.BEDROCK) { return; }
		BedrockBreaker.removePlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType() != Material.BEDROCK) { return; }
		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) { return; }
		if (!event.isDropItems() || !BedrockBreaker.shouldDrop(event.getPlayer())) { return; }
		Utils.dropBlockItem(event.getBlock(), event.getPlayer(), new ItemStack(Material.BEDROCK));
	}
}
