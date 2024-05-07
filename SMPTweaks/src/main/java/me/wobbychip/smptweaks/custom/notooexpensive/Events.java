package me.wobbychip.smptweaks.custom.notooexpensive;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.AnvilInventory;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPrepareAnvilEvent(PrepareAnvilEvent event) {
		World world = event.getInventory().getLocation().getWorld();
		if (!NoTooExpensive.tweak.getGameRuleBoolean(world)) { return; }
		int cost = event.getInventory().getRepairCost();

		for (HumanEntity player : event.getViewers()) {
			if (player.getGameMode() == GameMode.CREATIVE) { continue; }
			boolean flag = (cost > NoTooExpensive.MAXIMUM_REPAIR_COST);
			ReflectionUtils.setInstantBuild((Player) player, flag, true, false);
		}

		event.getInventory().setMaximumRepairCost(Integer.MAX_VALUE);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public static void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getInventory() instanceof AnvilInventory inventory)) { return; }
		if (event.getSlot() != 2) { return; }
		if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) { return; }
		if (!NoTooExpensive.tweak.getGameRuleBoolean(inventory.getLocation().getWorld())) { return; }
		if (inventory.getRepairCost() <= NoTooExpensive.MAXIMUM_REPAIR_COST) { return; }
		if (((Player) event.getWhoClicked()).getLevel() >= inventory.getRepairCost()) { return; }

		event.setCancelled(true);
		((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Main.DENY_SOUND_EFFECT, 1f, 1f);
		ReflectionUtils.setInstantBuild((Player) event.getWhoClicked(), false, true, false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		if (event.getInventory().getType() != InventoryType.ANVIL) { return; }
		((AnvilInventory) event.getInventory()).setMaximumRepairCost(Integer.MAX_VALUE);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		if (event.getInventory().getType() != InventoryType.ANVIL) { return; }
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) { return; }
		ReflectionUtils.setInstantBuild((Player) event.getPlayer(), false, true, false);
	}
}
