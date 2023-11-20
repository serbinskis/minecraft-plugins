package me.wobbychip.smptweaks.custom.removedatapackitems;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.LootGenerateEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onLootGenerateEvent(LootGenerateEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onLootGenerateEvent(event); }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getEntity().getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onItemSpawnEvent(event); }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getWhoClicked().getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onInventoryClickEvent(event); }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onPlayerInteractAtEntityEvent(event); }
	}
}
