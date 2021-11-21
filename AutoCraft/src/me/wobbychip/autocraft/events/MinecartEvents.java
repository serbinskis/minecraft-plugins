package me.wobbychip.autocraft.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import me.wobbychip.autocraft.Utils;

public class MinecartEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onVehicleInteractEvent(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();

		if ((entity.getType() == EntityType.MINECART_CHEST) && entity.hasMetadata("AutoCrafter")) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onItemSpawnEvent(ItemSpawnEvent event) {
		if (Utils.isDummyItem(event.getEntity().getItemStack())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onVehicleMoveEvent(VehicleMoveEvent event) {
		Vehicle vehicle = event.getVehicle();

		if ((vehicle.getType() == EntityType.MINECART_CHEST) && vehicle.hasMetadata("AutoCrafter")) {
			vehicle.setVelocity(new Vector());
			vehicle.teleport(event.getFrom());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onVehicleDamageEvent(VehicleDamageEvent event) {
		Vehicle vehicle = event.getVehicle();

		if ((vehicle.getType() == EntityType.MINECART_CHEST) && vehicle.hasMetadata("AutoCrafter")) {
			event.setDamage(0);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onVehicleDestroyEvent(VehicleDestroyEvent event) {
		Vehicle vehicle = event.getVehicle();

		if ((vehicle.getType() == EntityType.MINECART_CHEST) && vehicle.hasMetadata("AutoCrafter")) {
			event.setCancelled(true);
		}
	}
}
