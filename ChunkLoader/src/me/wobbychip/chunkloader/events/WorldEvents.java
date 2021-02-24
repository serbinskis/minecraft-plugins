package me.wobbychip.chunkloader.events;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import me.wobbychip.chunkloader.Main;
import me.wobbychip.chunkloader.Utilities;

public class WorldEvents implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		String chunkName = "chunks." + event.getWorld().getName() + "." + Utilities.CoordsToString(chunk.getX(), chunk.getZ());
		if (Main.ChunksConfig.getConfig().contains(chunkName)) {
			Utilities.DebugInfo("&9[ChunkLoader] Chunk (" + chunk.getX()*16 + "," + chunk.getZ()*16 + ") in world '" + event.getWorld().getName() + "' is unloading, while it should be force-loaded.");
		}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldUnload(WorldUnloadEvent event) {
    	String world = event.getWorld().getName();
    	FileConfiguration config = Main.ChunksConfig.getConfig();
 
    	if ((config.contains("chunks." + world)) && (config.getConfigurationSection("chunks." + world).getKeys(false).size() > 0)) {
    		event.setCancelled(true);
    		Utilities.DebugInfo("&9[ChunkLoader] World '" + world + "' tried to unload, but since it has force loaded chunks event was canceled.");
    	}
    }
}
