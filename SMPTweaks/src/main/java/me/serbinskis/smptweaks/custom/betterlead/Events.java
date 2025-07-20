package me.serbinskis.smptweaks.custom.betterlead;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.google.common.base.Predicate;
import io.papermc.paper.entity.Leashable;
import me.serbinskis.smptweaks.library.tinyprotocol.PacketEvent;
import me.serbinskis.smptweaks.library.tinyprotocol.PacketType;
import me.serbinskis.smptweaks.utils.PaperUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class Events implements Listener {
	private static final HashMap<Integer, Location> preventPacket = new HashMap<>();
	private static final HashMap<UUID, Player> holders = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!BetterLead.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (!(event.getRightClicked() instanceof Leashable leashable)) { return; }
		if (event.getPlayer().getInventory().getItem(event.getHand()).getType() != Material.LEAD) { return; }
		if (!PaperUtils.isLeashable(leashable)) { return; }
		event.setCancelled(true);

		TaskUtils.scheduleSyncDelayedTask(() -> {
			ItemStack item1 = event.getPlayer().getInventory().getItem(event.getHand());
			if (item1.getType() != Material.LEAD) { return; }

			boolean isLeashed = leashable.setLeashHolder(event.getPlayer());
			if (isLeashed && (event.getHand() == EquipmentSlot.HAND)) { event.getPlayer().swingMainHand(); }
			if (isLeashed && (event.getHand() == EquipmentSlot.OFF_HAND)) { event.getPlayer().swingOffHand(); }
			if ((event.getPlayer().getGameMode() != GameMode.CREATIVE) && isLeashed) { item1.setAmount(item1.getAmount()-1); }
		}, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityUnleashEvent(EntityUnleashEvent event) {
		if (!BetterLead.tweak.getGameRuleBoolean(event.getEntity().getWorld())) { return; }
		if (!(event.getEntity() instanceof Leashable entity)) { return; }
		if (!(entity.getLeashHolder() instanceof Player player)) { return; }
		if (event.getReason() != UnleashReason.DISTANCE) { return; }
		if (Utils.distance(player.getLocation(), entity.getLocation()) > BetterLead.LEASH_MAX_DISTANCE) { return; }
		if (Utils.isMovable(entity)) { BetterLead.setDeltaMovement(player, entity); }

		preventPacket.put(entity.getEntityId(), player.getLocation().clone());
		TaskUtils.scheduleSyncDelayedTask(() -> preventPacket.remove(entity.getEntityId()), 1L);
		event.setCancelled(true);
	}

	//This won't work, because paper forgot about if else's, fucking idiots
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerElytraBoostEvent(PlayerElytraBoostEvent event) {
		if (!BetterLead.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		Stream<Leashable> entityStream = event.getPlayer().getWorld().getEntities().stream().filter(Leashable.class::isInstance).map(Leashable.class::cast).filter(Leashable::isLeashed);
		entityStream.filter(e -> e.getLeashHolder().getUniqueId().equals(event.getPlayer().getUniqueId())).forEach(e -> holders.put(e.getUniqueId(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		if (event.getEntity().getItemStack().getType() != Material.LEAD) { return; }
		Location location = event.getLocation();

		for (Entity entity : location.getWorld().getNearbyEntities(location, 0.01, 0.01, 0.01)) {
			if (!(entity instanceof Leashable leashable)) { continue; }

			if (holders.containsKey(entity.getUniqueId())) {
				Player player = holders.remove(entity.getUniqueId());
				if (Utils.isMovable(entity)) { BetterLead.setDeltaMovement(player, entity); }
				leashable.setLeashHolder(player);
				preventPacket.put(entity.getEntityId(), player.getLocation().clone());
				TaskUtils.scheduleSyncDelayedTask(() -> preventPacket.remove(entity.getEntityId()), 1L);
			}

			event.setCancelled(true);
			break;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketLeashEvent(PacketEvent event) {
		if (event.getPacketType() != PacketType.SET_ENTITY_LINK) { return; }
		int[] entityIds = ReflectionUtils.getEntityLinkPacketIds(event.getPacket());
		if ((entityIds[1] > 0) || !preventPacket.containsKey(entityIds[0])) { return; }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketSoundEvent(PacketEvent event) {
		if (event.getPacketType() != PacketType.SOUND) { return; }
		Map.Entry<Sound, Location> bukkitSoundInfo = ReflectionUtils.getBukkitSoundInfo(event.getPacket());
		if (!bukkitSoundInfo.getKey().equals(Sound.ITEM_LEAD_BREAK)) { return; }
		Predicate<Location> filter = (location) -> Utils.distance(location, bukkitSoundInfo.getValue()) <= 0.21f; //Very bad precision, fucking stupid sound
		event.setCancelled(preventPacket.values().stream().anyMatch(filter));
	}
}
