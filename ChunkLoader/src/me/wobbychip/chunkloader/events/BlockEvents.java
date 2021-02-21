package me.wobbychip.chunkloader.events;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.wobbychip.chunkloader.ChunkLoader;
import me.wobbychip.chunkloader.Main;

public class BlockEvents implements Listener {
	@EventHandler(priority=EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event)  {
		if (event.isCancelled()) { return; }
		if (event.getBlock().getType() != Material.LODESTONE) { return; }
		if (!event.getItemInHand().getItemMeta().getLocalizedName().equals("chunk_loader")) { return; }
		Player player = event.getPlayer();

		if (!player.hasPermission("chunkloader.use")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.plugin.getConfig().getString("permissionMessage")));
			event.setCancelled(true);
			return;
		}

		new ChunkLoader(event.getBlock().getLocation(), event.getPlayer(), true);
	}

	@EventHandler(ignoreCancelled = true, priority=EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) { return; }
		Location location = event.getBlock().getLocation();

		ChunkLoader chunkLoader = new ChunkLoader(location);
		if (chunkLoader.Exists()) {
			chunkLoader.Remove(true);
			event.setDropItems(false);
		}
	}

    @EventHandler(priority=EventPriority.MONITOR)
	public void onBlockExplode(EntityExplodeEvent event) {
    	for (Block block : event.blockList()) {
    		ChunkLoader chunkLoader = new ChunkLoader(block.getLocation());
    		if (chunkLoader.Exists()) { chunkLoader.Remove(true); }
        }
    }

	@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if (event.getClickedBlock().getType() != Material.LODESTONE) { return; }
		ChunkLoader chunkLoader = new ChunkLoader(event.getClickedBlock().getLocation());
		if (!chunkLoader.Exists()) { return; }

		event.setCancelled(true);
		if (event.getHand() != EquipmentSlot.HAND) { return; }
		Player player = event.getPlayer();

		if (!player.hasPermission("chunkloader.use")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.plugin.getConfig().getString("permissionMessage")));
			return;
		}

		if (player.getUniqueId().equals(chunkLoader.getOwner()) || player.hasPermission("chunkloader.bypass")) {
			if (!chunkLoader.isOpened()) {
				chunkLoader.OpenInvenotry(player);
			} else {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.plugin.getConfig().getString("openedMessage")));
				return;
			}
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.plugin.getConfig().getString("notOwner")));
		}
	}
}
