package me.wobbychip.smptweaks.custom.autotrade;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;

import me.wobbychip.smptweaks.Main;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if (AutoTrade.traders.isTrader(event.getBlock())) { event.setCancelled(true); }
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		if (AutoTrade.allowBlockRecipeModification) { return; }

		InventoryType type = event.getInitiator().getType();
		if ((type != InventoryType.HOPPER) && (type != InventoryType.DROPPER)) { return; }

		Block source = event.getSource().getLocation().getBlock();
		if (AutoTrade.traders.isTrader(source)) { event.setCancelled(true); }
		if (event.isCancelled()) { return; } //Prevent further unnecessary code execution

		Block destination = event.getDestination().getLocation().getBlock();
		if (AutoTrade.traders.isTrader(destination)) { event.setCancelled(true); }
	}

	//WHO TF IS CANCELLING MY EVENTS
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		boolean isFakePlayer = event.getPlayer().getUniqueId().equals(AutoTrade.fakePlayer.getUniqueId());
		if (isFakePlayer) { event.setCancelled(false); }

		if (event.isCancelled()) { return; }
		if (event.getInventory().getType() != InventoryType.DISPENSER) { return; }

		InventoryHolder holder = (InventoryHolder) event.getInventory().getHolder();
		if (!(holder instanceof BlockInventoryHolder)) { return; }

		Block block = ((BlockInventoryHolder) holder).getBlock();
		if (!AutoTrade.traders.isTrader(block)) { return; }
		Villagers.releaseXp(block, event.getPlayer().getLocation());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplodeEvent(BlockExplodeEvent event) {
		for (Block block : event.blockList()) {
			if (block.getType() != Material.DISPENSER) { continue; }
			Villagers.releaseXp(block, block.getLocation().clone().add(0.5, 0.5, 0.5));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType() != Material.DISPENSER) { return; }
		Villagers.releaseXp(event.getBlock(), event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof ItemFrame)) { return; }
		ItemFrame frame = (ItemFrame) event.getEntity();
		if (frame.getItem().getType() != Material.NETHER_STAR) { return; }
		Block block = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
		if (block.getType() != Material.DISPENSER) { return; }

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				Block trader = AutoTrade.traders.getTrader(block);
				if (trader != null) { return; }
				Villagers.releaseXp(block, block.getLocation().clone().add(0.5, 1.2, 0.5));
			}
		}, 1L);
	}
}