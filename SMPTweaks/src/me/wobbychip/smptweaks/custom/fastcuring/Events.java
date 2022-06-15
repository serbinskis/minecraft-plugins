package me.wobbychip.smptweaks.custom.fastcuring;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import me.wobbychip.smptweaks.Main;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() != EntityType.ZOMBIE_VILLAGER) { return; }
		ZombieVillager villager = (ZombieVillager) event.getRightClicked();
		if (villager.isConverting()) { return; }

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
            public void run() {
            	if (villager.isConverting()) { villager.setConversionTime(FastCuring.intervalTicks); }
            }
        }, 1);
	}
}
