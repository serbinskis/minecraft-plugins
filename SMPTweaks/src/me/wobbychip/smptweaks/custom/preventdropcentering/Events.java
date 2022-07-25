package me.wobbychip.smptweaks.custom.preventdropcentering;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.Vector;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	public double ITEM_HEIGHT = 0.25F;
	public double ITEM_SPAWN_OFFSET = 0.25F;
	public double DISTANCE = 0.5F;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getPlayer() == null) { return; }
		Location location = event.getBlock().getLocation().add(.5, .5, .5);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        public void run() {
	        	for (Entity entity : location.getWorld().getNearbyEntities(location, DISTANCE, DISTANCE, DISTANCE)) {
	                if (entity instanceof Item) {
	                	double posX = Utils.afterDecimal(entity.getLocation().getX());
	                	double posY = Utils.afterDecimal(entity.getLocation().getY());
	                	double posZ = Utils.afterDecimal(entity.getLocation().getZ());

	                	double velX = entity.getVelocity().getX();
	                	double velY = entity.getVelocity().getY();
	                	double velZ = entity.getVelocity().getZ();

	                	if ((velX == 0.0F) && (velY == 0.0F) && (velZ == 0.0F) && (posX == 0.5F) && (posY == 0.5F) && (posZ == 0.5F)) {
	                		popResource(entity, location);
	                	}
	                }
	            }
	        }
	    }, 1L);
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
