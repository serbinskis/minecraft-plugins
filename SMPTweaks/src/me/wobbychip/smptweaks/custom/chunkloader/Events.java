package me.wobbychip.smptweaks.custom.chunkloader;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplodeEvent(BlockExplodeEvent event) {
		for (Block block : event.blockList()) {
			if (block.getType() != Material.LODESTONE) { continue; }
			ChunkLoader.manager.removeLoader(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType() != Material.LODESTONE) { return; }
		ChunkLoader.manager.removeLoader(event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
		Collection<Block> blocks = Utils.getNearestBlocks(event.getBlock().getLocation(), Material.LODESTONE, 2);
		ChunkLoader.manager.updateLoader(event.getBlock());

		for (Block block : blocks) {
			ChunkLoader.manager.updateLoader(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof ItemFrame)) { return; }
		ItemFrame frame = (ItemFrame) event.getEntity();
		if (frame.getAttachedFace() != BlockFace.DOWN) { return; }

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				if (frame.getItem().getType() == Material.NETHER_STAR) { return; }
				Block block = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
				ChunkLoader.manager.removeLoader(block);
			}
		}, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
		if (!(event.getRightClicked() instanceof ItemFrame)) { return; }
		ItemFrame frame = (ItemFrame) event.getRightClicked();
		if (frame.getAttachedFace() != BlockFace.DOWN) { return; }

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				if (frame.getItem().getType() != Material.NETHER_STAR) { return; }
				Block block = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
				ChunkLoader.manager.addLoader(block, true);
			}
		}, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if (event.getHand() != EquipmentSlot.HAND) { return; }

		ItemStack mainhand = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);
		if ((mainhand == null) || (mainhand.getType() != Material.AIR)) { return; }

		ItemStack offhand = event.getPlayer().getInventory().getItem(EquipmentSlot.OFF_HAND);
		if ((offhand == null) || (offhand.getType() != Material.AIR)) { return; }

		Loader loader = ChunkLoader.manager.getLoader(event.getClickedBlock());
		if (loader == null) { return; }

		Border border = ChunkLoader.manager.getBorder(event.getPlayer());
		if ((border != null) && !border.equals(loader.getBorder())) { border.removePlayer(event.getPlayer()); }
		loader.getBorder().togglePlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeathEvent(EntityDeathEvent event) {
		if (event.getEntity().getType() != EntityType.SHULKER) { return; }
		if (!PersistentUtils.hasPersistentDataBoolean(event.getEntity(), ChunkLoader.isChunkLoader)) { return; }
		event.getDrops().clear();
		event.setDroppedExp(0);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.getEntity().getType() != EntityType.SHULKER) { return; }
		if (!PersistentUtils.hasPersistentDataBoolean(event.getEntity(), ChunkLoader.isChunkLoader)) { return; }
		event.setCancelled(true);
	}
}