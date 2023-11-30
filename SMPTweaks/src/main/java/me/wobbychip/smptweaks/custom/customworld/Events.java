package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkLoadEvent(ChunkLoadEvent event) {
		if (!event.isNewChunk()) { return; }
		if (!PersistentUtils.hasPersistentDataString(event.getWorld(), CustomWorld.TAG_CUSTOM_WORLD)) { return; }

		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getWorld(), CustomWorld.TAG_CUSTOM_WORLD));
		if ((type != CustomWorld.Type.END) && (type != CustomWorld.Type.VOID)) { return; }
		Utils.fillChunk(event.getChunk(), Material.AIR, true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldInitEvent(WorldInitEvent event) {
		if (!PersistentUtils.hasPersistentDataString(event.getWorld(), CustomWorld.TAG_CUSTOM_WORLD)) { return; }
		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getWorld(), CustomWorld.TAG_CUSTOM_WORLD));
		if (type != CustomWorld.Type.VOID) { return; }

		ReflectionUtils.setCustomDimension(event.getWorld(), null, World.Environment.THE_END, null, null, null, null, null, null, null, null, 0, null, null, null, null, null, null);
	}
}
