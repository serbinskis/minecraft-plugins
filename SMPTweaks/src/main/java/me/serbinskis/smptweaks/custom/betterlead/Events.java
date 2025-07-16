package me.serbinskis.smptweaks.custom.betterlead;

import me.serbinskis.smptweaks.library.tinyprotocol.PacketEvent;
import me.serbinskis.smptweaks.library.tinyprotocol.PacketType;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class Events implements Listener {
	public HashMap<Integer, Entity> preventPacket = new HashMap<>();
	public HashMap<UUID, Player> holders = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!BetterLead.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (!(event.getRightClicked() instanceof LivingEntity entity)) { return; }
		ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
		if (item.getType() != Material.LEAD) { return; }

		if (!BetterLead.custom.isEmpty() && !BetterLead.custom.contains(entity.getType().toString())) { return; }
		if (!Utils.isLeashable(entity)) { return; }
		event.setCancelled(true);

		TaskUtils.scheduleSyncDelayedTask(() -> {
			if (!(event.getRightClicked() instanceof LivingEntity entity1)) { return; }
			ItemStack item1 = event.getPlayer().getInventory().getItem(event.getHand());
			if (item1.getType() != Material.LEAD) { return; }
			if (!Utils.isLeashable(entity1)) { return; }

			entity1.setLeashHolder(event.getPlayer());
			boolean isLeashed = entity1.setLeashHolder(event.getPlayer());

			if (isLeashed && (event.getHand() == EquipmentSlot.HAND)) { event.getPlayer().swingMainHand(); }
			if (isLeashed && (event.getHand() == EquipmentSlot.OFF_HAND)) { event.getPlayer().swingOffHand(); }

			if ((event.getPlayer().getGameMode() != GameMode.CREATIVE) && isLeashed) {
				item1.setAmount(item1.getAmount()-1);
			}
		}, 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityUnleashEvent(EntityUnleashEvent event) {
		if (!BetterLead.tweak.getGameRuleBoolean(event.getEntity().getWorld())) { return; }
		if ((event.getReason() != UnleashReason.DISTANCE)) { return; }
		if (!(event.getEntity() instanceof LivingEntity entity)) { return; }
		if (!(entity.getLeashHolder() instanceof Player player)) { return; }

		if (Utils.distance(player.getLocation(), event.getEntity().getLocation()) > BetterLead.maxDistance) { return; }
		PersistentUtils.setPersistentDataBoolean(event.getEntity(), BetterLead.TAG_IS_UNBREAKABLE_LEASH, true);
		holders.put(event.getEntity().getUniqueId(), player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		if (event.getEntity().getItemStack().getType() != Material.LEAD) { return; }
		Location location = event.getLocation();

		for (Entity entity : location.getWorld().getNearbyEntities(location, 0.01, 0.01, 0.01)) {
			if (!(entity instanceof LivingEntity)) { continue; }
			if (!PersistentUtils.hasPersistentDataBoolean(entity, BetterLead.TAG_IS_UNBREAKABLE_LEASH)) { continue; }
			PersistentUtils.removePersistentData(entity, BetterLead.TAG_IS_UNBREAKABLE_LEASH);

			if (holders.containsKey(entity.getUniqueId())) {
				Player player = holders.get(entity.getUniqueId());
				if (Utils.isMovable(entity)) { BetterLead.setDeltaMovement(player, entity); }
				((LivingEntity) entity).setLeashHolder(player);
				int entityId = ReflectionUtils.getEntityId(entity);
				preventPacket.put(entityId, entity);
				holders.remove(entity.getUniqueId());
				TaskUtils.scheduleSyncDelayedTask(() -> preventPacket.remove(entityId), 2);
			}

			event.setCancelled(true);
			break;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketEvent(PacketEvent event) {
		if (event.getPacketType() != PacketType.SET_ENTITY_LINK) { return; }
		int[] entityIds = ReflectionUtils.getEntityLinkPacketIds(event.getPacket());
		if ((entityIds[1] > 0) || !preventPacket.containsKey(entityIds[0])) { return; }
		event.setCancelled(true);
	}

	//Have to somehow prevent leash breaking sound, sound goes to all players, so idk how to indentify correct sound

	/*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketEvent12312(PacketEvent event) {
		if ((event.getPacketType() == PacketType.SOUND) && (preventSoundPackets.remove(event.getPlayer().getUniqueId()) != null)) {
			Sound sound = ReflectionUtils.getBukkitSound(event.getPacket());
			if (sound.equals(Sound.ENTITY_PLAYER_LEVELUP)) { event.setCancelled(true); }
		}

		if ((event.getPacketType() == PacketType.SET_EXPERIENCE) && (preventExperiencePackets.remove(event.getPlayer().getUniqueId()) != null)) {
			event.setCancelled(true);
		}
	}*/
}
