package me.wobbychip.smptweaks.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorldEditUtils {
	public static boolean isWorldEdit = isWorldEdit();

	public static boolean isWorldEdit() {
		try {
			return (com.sk89q.worldedit.WorldEdit.class != null);
		} catch (Exception e) { return false; }
	}

	public static List<Block> getSelectedBlocks(Player player) {
		if (!WorldEditUtils.isWorldEdit) { return null; }
		com.sk89q.worldedit.WorldEdit worldEdit = com.sk89q.worldedit.WorldEdit.getInstance();

		com.sk89q.worldedit.LocalSession worldEditSession = worldEdit.getSessionManager().findByName(player.getName());
		if (worldEditSession == null) { return null; }
		if (worldEditSession.getSelectionWorld() == null) { return null; }

		com.sk89q.worldedit.regions.RegionSelector regionSelector = worldEditSession.getRegionSelector(worldEditSession.getSelectionWorld());
		if (!regionSelector.isDefined()) { return null;  }

		try {
			com.sk89q.worldedit.regions.Region region = regionSelector.getRegion();
			World world = Bukkit.getWorld(Objects.requireNonNull(region.getWorld()).getName());
			List<Block> blocks = new ArrayList<>();
            region.iterator().forEachRemaining(e -> blocks.add(new Location(world, e.getX(), e.getY(), e.getZ()).getBlock()));
			return blocks;
		} catch (com.sk89q.worldedit.IncompleteRegionException e) {
			e.printStackTrace();
			return null;
		}
	}
}
