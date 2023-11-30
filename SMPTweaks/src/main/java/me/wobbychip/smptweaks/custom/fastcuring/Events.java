package me.wobbychip.smptweaks.custom.fastcuring;

import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() != EntityType.ZOMBIE_VILLAGER) { return; }
		if (!FastCuring.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		ZombieVillager villager = (ZombieVillager) event.getRightClicked();
		if (villager.isConverting()) { return; }

		TaskUtils.scheduleSyncDelayedTask(() -> {
			if (!villager.isConverting()) { return; }
			OfflinePlayer player = villager.getConversionPlayer();
			villager.setConversionTime(FastCuring.intervalTicks);
			villager.setConversionPlayer(player);
		}, 1);
	}
}
