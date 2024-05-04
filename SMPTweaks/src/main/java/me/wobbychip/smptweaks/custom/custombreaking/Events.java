package me.wobbychip.smptweaks.custom.custombreaking;

import me.wobbychip.smptweaks.custom.custombreaking.breaking.CustomBlock;
import me.wobbychip.smptweaks.custom.custombreaking.breaking.CustomBreaker;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamageEvent(BlockDamageEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) { return; }
		CustomBreaker.addPlayer(event.getPlayer(), event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamageAbortEvent(BlockDamageAbortEvent event) {
		CustomBreaker.removePlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) { return; }
		CustomBlock customBlock = CustomBreaker.getCustom(event.getBlock());
		if ((customBlock == null) || !event.isDropItems() || !customBlock.shouldDropItem(event.getBlock(), event.getPlayer())) { return; }
		if (!customBlock.shouldDropExp(event.getBlock(), event.getPlayer())) { event.setExpToDrop(0); }
		customBlock.onBlockBreakEvent(event);
		ReflectionUtils.dropBlockItem(event.getBlock(), event.getPlayer(), customBlock.getDropItem(event.getBlock(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		CustomBlock customBlock = CustomBreaker.getCustom(event.getBlock());
		if (customBlock != null) { customBlock.onBlockPlaceEvent(event); }
	}
}
