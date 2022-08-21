package me.wobbychip.smptweaks.custom.notooexpensive;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import me.wobbychip.smptweaks.utils.ReflectionUtils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPrepareAnvilEvent(PrepareAnvilEvent event) {
		for (HumanEntity player : event.getViewers()) {
			if (((Player) player).getGameMode() == GameMode.CREATIVE) { continue; }
			boolean flag = event.getInventory().getRepairCost() > NoTooExpensive.MAXIMUM_REPAIR_COST;
			ReflectionUtils.setInstantBuild((Player) player, flag, true, false);
		}

		event.getInventory().setMaximumRepairCost(Integer.MAX_VALUE);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		if (event.getInventory().getType() != InventoryType.ANVIL) { return; }
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) { return; }
		ReflectionUtils.setInstantBuild((Player) event.getPlayer(), false, true, false);
	}
}
