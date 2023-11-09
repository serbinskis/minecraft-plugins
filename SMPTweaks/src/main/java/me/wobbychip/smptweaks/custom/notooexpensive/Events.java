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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPrepareAnvilEvent(PrepareAnvilEvent event) {
		World world = event.getInventory().getLocation().getWorld();
		if (!NoTooExpensive.tweak.getGameRuleBoolean(world)) { return; }

		for (HumanEntity player : event.getViewers()) {
			if (((Player) player).getGameMode() == GameMode.CREATIVE) { continue; }
			boolean flag = event.getInventory().getRepairCost() > NoTooExpensive.MAXIMUM_REPAIR_COST;
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
		if (((Player) event.getWhoClicked()).getLevel() < inventory.getRepairCost()) { return; }

		((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Main.DENY_SOUND_EFFECT, 1.0f, 1.0f);
		ReflectionUtils.setInstantBuild((Player) event.getWhoClicked(), false, true, false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		if (event.getInventory().getType() != InventoryType.ANVIL) { return; }
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) { return; }
		ReflectionUtils.setInstantBuild((Player) event.getPlayer(), false, true, false);
	}
}
