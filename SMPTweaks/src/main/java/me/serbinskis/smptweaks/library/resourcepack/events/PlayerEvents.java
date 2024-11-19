package me.serbinskis.smptweaks.library.resourcepack.events;

import me.serbinskis.smptweaks.library.resourcepack.ResourcePacks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerEvents implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		for (Map.Entry<String, byte[]> resourcePack : ResourcePacks.resourcePacks.entrySet()) {
			event.getPlayer().addResourcePack(UUID.nameUUIDFromBytes(resourcePack.getValue()), resourcePack.getKey(), resourcePack.getValue(), ResourcePacks.RESOURCE_PACK_PROMPT, true);
		}
	}
}