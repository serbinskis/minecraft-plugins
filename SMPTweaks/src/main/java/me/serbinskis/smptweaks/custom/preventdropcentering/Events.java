package me.serbinskis.smptweaks.custom.preventdropcentering;

import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Events implements Listener {
	public double ITEM_HEIGHT = 0.25F;
	public double ITEM_SPAWN_OFFSET = 0.25F;
	private final Set<Location> brokenBlocks = new HashSet<>(); // Tracks blocks broken on this tick

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		Location blockLoc = event.getBlock().getLocation();
		brokenBlocks.add(blockLoc);

		// Remove from tracking at the end of the tick (2L - BECAUSE OF SOME SHITTY ANOTHER PLUGIN)
		TaskUtils.scheduleSyncDelayedTask(() -> brokenBlocks.remove(blockLoc), 2L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		Item entity = event.getEntity();
		Location blockLoc = entity.getLocation().getBlock().getLocation();

		// Only process if this item spawned inside a block broken by a player:
		if (!brokenBlocks.contains(blockLoc)) { return; }

		double posX = Utils.afterDecimal(entity.getLocation().getX());
		double posY = Utils.afterDecimal(entity.getLocation().getY());
		double posZ = Utils.afterDecimal(entity.getLocation().getZ());

		if ((posX != 0.5F) || (posY != 0.5F) || (posZ != 0.5F)) { return; }

		// This is because of intervention of another plugin, aka, just doing popResource won't work
		TaskUtils.scheduleSyncDelayedTask(() -> popResource(entity, entity.getLocation()), 0L);
	}

	public void popResource(Entity entity, Location location) {
		double f = (ITEM_HEIGHT / 2.0F);
		double d0 = location.getX() + Utils.randomRange(-ITEM_SPAWN_OFFSET, ITEM_SPAWN_OFFSET);
		double d1 = location.getY() + Utils.randomRange(-ITEM_SPAWN_OFFSET, ITEM_SPAWN_OFFSET) - f;
		double d2 = location.getZ() + Utils.randomRange(-ITEM_SPAWN_OFFSET, ITEM_SPAWN_OFFSET);

		entity.teleport(new Location(location.getWorld(), d0, d1, d2));
		entity.setVelocity(new Vector(Math.random()*0.2F-0.1F, 0.2F, Math.random()*0.2F-0.1F));
	}
}

