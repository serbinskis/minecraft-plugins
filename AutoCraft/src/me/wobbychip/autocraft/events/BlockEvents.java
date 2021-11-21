package me.wobbychip.autocraft.events;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.wobbychip.autocraft.Main;
import me.wobbychip.autocraft.crafters.CustomInventoryCrafting;

public class BlockEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (event.getBlock().getType() == Material.CRAFTING_TABLE) {
			Main.manager.createWorkbench(event.getBlock().getLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		Location location = event.getBlock().getLocation();
		Main.manager.remove(location);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockExplodeEvent(BlockExplodeEvent event) {
		for (Block block : event.blockList()) {
			Main.manager.remove(block.getLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		for (Block block : event.blockList()) {
			Main.manager.remove(block.getLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonExtendEvent(BlockPistonExtendEvent event) {
		BlockFace face = event.getDirection();
		Set<CustomInventoryCrafting> moveThese = new HashSet<CustomInventoryCrafting>();
		for (Block block : event.getBlocks()) {
			CustomInventoryCrafting crafting = Main.manager.get(block.getLocation());
			if (crafting != null) {
				moveThese.add(crafting);
			}
		}
		for (CustomInventoryCrafting crafting : moveThese) {
			crafting.move(crafting.getLocation().getBlock().getRelative(face).getLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonRetractEvent(BlockPistonRetractEvent event) {
		BlockFace face = event.getDirection();
		Set<CustomInventoryCrafting> moveThese = new HashSet<CustomInventoryCrafting>();
		for (Block block : event.getBlocks()) {
			CustomInventoryCrafting crafting = Main.manager.get(block.getLocation());
			if (crafting != null) {
				moveThese.add(crafting);
			}
		}
		for (CustomInventoryCrafting crafting : moveThese) {
			crafting.move(crafting.getLocation().getBlock().getRelative(face).getLocation());
		}
	}
}
