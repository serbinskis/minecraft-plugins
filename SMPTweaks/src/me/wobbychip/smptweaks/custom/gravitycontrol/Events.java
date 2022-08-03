package me.wobbychip.smptweaks.custom.gravitycontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Events implements Listener {
	public List<UUID> uuids = new ArrayList<>();
	public double HORIZONTAL_COEFFICIENT = 1.46D;
	public double VERTICAL_COEFFICIENT = -2.4D;

	public Set<Vector> DIRECTIONS = Set.of(
		new Vector(0, 0, -1),
		new Vector(1, 0, 0),
		new Vector(0, 0, 1),
		new Vector(-1, 0, 0)
	);

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.getTo() == Material.AIR) { return; }
		if (!(event.getEntity() instanceof FallingBlock)) { return; }

		FallingBlock falling = (FallingBlock) event.getEntity();
		BoundingBox boundingBox = falling.getBoundingBox().expand(-0.01D);

		for (Vector direction : DIRECTIONS) {
			Location location = event.getBlock().getLocation().add(direction);
			if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) { continue; }

			Block block = location.getBlock();
			if ((block.getType() != Material.END_PORTAL) || !block.getBoundingBox().overlaps(boundingBox)) { continue; }
			Vector velocity = falling.getVelocity();

			if ((velocity.getX() == 0) && (velocity.getZ() == 0)) {
				location = falling.getLocation().add(direction.getX() * 0.25D, 0.05D, direction.getZ() * 0.25D);
				falling = location.getWorld().spawnFallingBlock(location, falling.getBlockData());
			} else {
				velocity = new Vector(velocity.getX() * HORIZONTAL_COEFFICIENT, velocity.getY() * VERTICAL_COEFFICIENT, velocity.getZ() * HORIZONTAL_COEFFICIENT);
				location = falling.getLocation().add(direction.getX() * 0.25D, direction.getY() * 0.25D, direction.getZ() * 0.25D);
				falling = falling.getWorld().spawnFallingBlock(location, falling.getBlockData());
				falling.setVelocity(velocity);
			}

			uuids.add(falling.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityPortalExitEvent(EntityPortalExitEvent event) {
		if (event.getEntity().getType() != EntityType.FALLING_BLOCK) { return; }
		if (!event.getTo().getWorld().getEnvironment().equals(Environment.THE_END)) { return; }
		if (!uuids.contains(event.getEntity().getUniqueId())) { return; }

		uuids.remove(event.getEntity().getUniqueId());
		event.setTo(event.getTo().add(0.5, 0, 0.5));
	}
}
