package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkLoadEvent(ChunkLoadEvent event) {
		if (!event.isNewChunk()) { return; }
		if (!PersistentUtils.hasPersistentDataString(event.getWorld(), CustomWorld.CUSTOM_WORLD_TAG)) { return; }
		Utils.fillChunk(event.getChunk(), Material.AIR, true);
	}
}
