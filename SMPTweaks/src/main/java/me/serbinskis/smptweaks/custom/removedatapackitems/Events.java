package me.serbinskis.smptweaks.custom.removedatapackitems;

import me.serbinskis.smptweaks.custom.removedatapackitems.datapacks.Incendium;
import me.serbinskis.smptweaks.custom.removedatapackitems.datapacks.Stellarity;
import me.serbinskis.smptweaks.utils.TaskUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.LootGenerateEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onLootGenerateEvent(LootGenerateEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onLootGenerateEvent(event); }
		if (RemoveDatapackItems.stellarity) { Stellarity.onLootGenerateEvent(event); }
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getEntity().getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onItemSpawnEvent(event); }
		if (RemoveDatapackItems.stellarity) { Stellarity.onItemSpawnEvent(event); }
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntitySpawnEvent(EntitySpawnEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getEntity().getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onEntitySpawnEvent(event); }
		if (RemoveDatapackItems.stellarity) { Stellarity.onEntitySpawnEvent(event); }
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (event.getPlayer().hasPlayedBefore()) { return; }

		if (RemoveDatapackItems.incendium) { TaskUtils.scheduleSyncDelayedTask(() -> Incendium.onPlayerJoinEvent(event), 2L); }
		if (RemoveDatapackItems.stellarity) { TaskUtils.scheduleSyncDelayedTask(() -> Stellarity.onPlayerJoinEvent(event), 2L); }
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getWhoClicked().getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onInventoryClickEvent(event); }
		if (RemoveDatapackItems.stellarity) { Stellarity.onInventoryClickEvent(event); }
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onPlayerInteractAtEntityEvent(event); }
		if (RemoveDatapackItems.stellarity) { Stellarity.onPlayerInteractAtEntityEvent(event); }
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onChunkLoadEvent(ChunkLoadEvent event) {
		if (!event.isNewChunk()) { return; }
		if (!RemoveDatapackItems.tweak.getGameRuleBoolean(event.getWorld())) { return; }
		if (RemoveDatapackItems.incendium) { Incendium.onChunkLoadEvent(event); }
		if (RemoveDatapackItems.stellarity) { Stellarity.onChunkLoadEvent(event); }
	}
}
