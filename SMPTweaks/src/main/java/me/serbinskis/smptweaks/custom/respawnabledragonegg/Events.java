package me.serbinskis.smptweaks.custom.respawnabledragonegg;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeathEvent(EntityDeathEvent event) {
		if (!event.getEntityType().equals(EntityType.ENDER_DRAGON)) { return; }
		if (!event.getEntity().getWorld().getEnvironment().equals(Environment.THE_END)) { return; }
		if (!event.getEntity().getWorld().getEnderDragonBattle().hasBeenPreviouslyKilled()) { return; }

		World world = event.getEntity().getWorld();
		Location location = new Location(world, 0, 0, 0);
		location.setY(location.getWorld().getHighestBlockYAt(0, 0) + 1);
		Block block = world.getBlockAt(location);
		block.setType(Material.DRAGON_EGG);
	}
}
