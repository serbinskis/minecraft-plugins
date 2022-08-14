package me.wobbychip.smptweaks.custom.betterlead;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

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

		if (Utils.distance(holder.getLocation(), event.getEntity().getLocation()) > BetterLead.MAX_DISTANCE) { return; }
		PersistentUtils.setPersistentDataBoolean(event.getEntity(), BetterLead.isUnbreakableLeash, true);
		holders.put(event.getEntity().getUniqueId(), (Player) holder);
		Utils.sendMessage(BetterLead.tickCount + " -> 1");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof LivingEntity)) { return; }
		ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
		if (item.getType() != Material.LEAD) { return; }

		LivingEntity entity = (LivingEntity) event.getRightClicked();
		if (!BetterLead.isLeashable(entity) || entity.isLeashed()) { return; }

		boolean isLeashed = entity.setLeashHolder(event.getPlayer());
		if (isLeashed) { event.setCancelled(true); }

		if ((event.getPlayer().getGameMode() != GameMode.CREATIVE) && isLeashed) {
			item.setAmount(item.getAmount()-1);
		}
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
				Utils.sendMessage(BetterLead.tickCount + " -> 2");
				Player player = holders.get(entity.getUniqueId());
				BetterLead.setDeltaMovement(player, (LivingEntity) entity);
				((LivingEntity) entity).setLeashHolder(player);
				BetterLead.updateLeash.add(Map.entry(player, (LivingEntity) entity));
				holders.remove(entity.getUniqueId());
			}

			event.setCancelled(true);
			break;
		}
	}
}
