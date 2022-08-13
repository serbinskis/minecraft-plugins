package me.wobbychip.smptweaks.custom.chunkloader.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.utils.PersistentUtils;

public class EntityEvents implements Listener {
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
}