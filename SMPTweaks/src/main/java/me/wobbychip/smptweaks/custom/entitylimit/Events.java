package me.wobbychip.smptweaks.custom.entitylimit;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCreatureSpawnn(CreatureSpawnEvent event) {
		//Return if disabled or spawn reason is excluded
		if (EntityLimit.excludeReason.contains(event.getSpawnReason().toString())) { return; }

		//Get nearest player to entity
		LivingEntity entity = event.getEntity();
		Player player = Utils.getNearetPlayer(entity.getLocation());

		//Check if player has bypass permissions
		if ((player != null) && player.hasPermission("entitylimit.bypass")) { return; }

		//Get entity count
		int nearbyEntities = Utils.getNearbyEntities(entity.getLocation(), entity.getType(), EntityLimit.maximumDistance, true).size();
		if (nearbyEntities < EntityLimit.limit) { return; }
		event.setCancelled(true); //Cancel entity if count is over limit

		if (player == null) { return; }
		String replacedMessage = EntityLimit.tooManyEntity.replace("%value%", String.valueOf(EntityLimit.limit));
		Utils.sendActionMessage(player, replacedMessage);
	}
}
