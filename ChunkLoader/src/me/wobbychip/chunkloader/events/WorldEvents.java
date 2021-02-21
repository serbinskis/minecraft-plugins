package me.wobbychip.chunkloader.events;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

import me.wobbychip.chunkloader.Main;
import me.wobbychip.chunkloader.Utilities;

public class WorldEvents implements Listener {
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
