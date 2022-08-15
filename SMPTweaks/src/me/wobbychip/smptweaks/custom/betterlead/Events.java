package me.wobbychip.smptweaks.custom.betterlead;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	public HashMap<UUID, Player> holders = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityUnleashEvent(EntityUnleashEvent event) {
		if ((event.getReason() != UnleashReason.DISTANCE)) { return; }
		if (!(event.getEntity() instanceof LivingEntity)) { return; }
		Entity holder = ((LivingEntity) event.getEntity()).getLeashHolder();
		if (!(holder instanceof Player)) { return; }

		if (Utils.distance(holder.getLocation(), event.getEntity().getLocation()) > BetterLead.maxDistance) { return; }
		PersistentUtils.setPersistentDataBoolean(event.getEntity(), BetterLead.isUnbreakableLeash, true);
		holders.put(event.getEntity().getUniqueId(), (Player) holder);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof LivingEntity)) { return; }
		ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
		if (item.getType() != Material.LEAD) { return; }

		LivingEntity entity = (LivingEntity) event.getRightClicked();
		if ((entity.getType() != EntityType.VILLAGER) && BetterLead.onlyVillagers) { return; }
		if (!BetterLead.isLeashable(entity)) { return; }
		event.setCancelled(true);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				if (!(event.getRightClicked() instanceof LivingEntity)) { return; }
				ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
				if (item.getType() != Material.LEAD) { return; }

				LivingEntity entity = (LivingEntity) event.getRightClicked();
				if (!BetterLead.isLeashable(entity)) { return; }

				entity.setLeashHolder(event.getPlayer());
				boolean isLeashed = entity.setLeashHolder(event.getPlayer());

				if (isLeashed && (event.getHand() == EquipmentSlot.HAND)) { event.getPlayer().swingMainHand(); }
				if (isLeashed && (event.getHand() == EquipmentSlot.OFF_HAND)) { event.getPlayer().swingOffHand(); }

				if ((event.getPlayer().getGameMode() != GameMode.CREATIVE) && isLeashed) {
					item.setAmount(item.getAmount()-1);
				}
			}
		}, 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		if (event.getEntity().getItemStack().getType() != Material.LEAD) { return; }
		Location location = event.getLocation();

		for (Entity entity : location.getWorld().getNearbyEntities(location, 0.01, 0.01, 0.01)) {
			if (!(entity instanceof LivingEntity)) { continue; }
			if (!PersistentUtils.hasPersistentDataBoolean(entity, BetterLead.isUnbreakableLeash)) { continue; }
			PersistentUtils.removePersistentData(entity, BetterLead.isUnbreakableLeash);

			if (holders.containsKey(entity.getUniqueId())) {
				Player player = holders.get(entity.getUniqueId());
				BetterLead.setDeltaMovement(player, (LivingEntity) entity);
				((LivingEntity) entity).setLeashHolder(player);
				BetterLead.preventPacket.add(entity.getUniqueId());
				holders.remove(entity.getUniqueId());

				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
					public void run() {
						BetterLead.preventPacket.remove(entity.getUniqueId());
					}
				}, 1);
			}

			event.setCancelled(true);
			break;
		}
	}
}
