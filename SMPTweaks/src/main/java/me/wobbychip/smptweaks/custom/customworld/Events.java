package me.wobbychip.smptweaks.custom.customworld;

import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkLoadEvent(ChunkLoadEvent event) {
		if (!event.isNewChunk()) { return; }
		if (!PersistentUtils.hasPersistentDataString(event.getWorld(), CustomWorld.CUSTOM_WORLD_TAG)) { return; }

		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getWorld(), CustomWorld.CUSTOM_WORLD_TAG));
		if ((type != CustomWorld.Type.VOID) && (type != CustomWorld.Type.END)) { return; }
		Utils.fillChunk(event.getChunk(), Material.AIR, true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldInitEvent(WorldInitEvent event) {
		if (!PersistentUtils.hasPersistentDataString(event.getWorld(), CustomWorld.CUSTOM_WORLD_TAG)) { return; }
		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getWorld(), CustomWorld.CUSTOM_WORLD_TAG));
		if (type != CustomWorld.Type.END) { return; }

		ReflectionUtils.setCustomDimension(event.getWorld(), null, World.Environment.THE_END, null, null, null, null, null, null, null, null, 0, null, null, null, null, null, null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		if (!PersistentUtils.hasPersistentDataString(event.getPlayer().getWorld(), CustomWorld.CUSTOM_WORLD_TAG)) { return; }
		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getPlayer().getWorld(), CustomWorld.CUSTOM_WORLD_TAG));
		if (type != CustomWorld.Type.END) { return; }

		Location location = event.getPlayer().getLocation().clone();
		ServerPlayer player = ReflectionUtils.getEntityPlayer(event.getPlayer());

		TaskUtils.scheduleSyncDelayedTask(() -> {
			player.setPosRaw(location.getX(), location.getY(), location.getZ());
			event.getPlayer().saveData();
		}, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		if (!PersistentUtils.hasPersistentDataString(event.getTo().getWorld(), CustomWorld.CUSTOM_WORLD_TAG)) { return; }
		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getTo().getWorld(), CustomWorld.CUSTOM_WORLD_TAG));
		if (type != CustomWorld.Type.END) { return; }

		event.setTo(event.getTo().clone().add(0, 0.5, 0));
	}

	//There is issue when players fall through block, idk why
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		if (!PersistentUtils.hasPersistentDataString(event.getRespawnLocation().getWorld(), CustomWorld.CUSTOM_WORLD_TAG)) { return; }
		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(event.getRespawnLocation().getWorld(), CustomWorld.CUSTOM_WORLD_TAG));
		if (type != CustomWorld.Type.END) { return; }

		event.setRespawnLocation(event.getRespawnLocation().clone().add(0, 0.5, 0));
	}
}
