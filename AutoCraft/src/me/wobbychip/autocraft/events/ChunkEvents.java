package me.wobbychip.autocraft.events;

import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.autocraft.Main;
import me.wobbychip.autocraft.crafters.CraftInventoryLoader;

public class ChunkEvents implements Listener {
	@EventHandler()
	public void onChunkLoadEvent(ChunkLoadEvent event) {
		//Utils.sendMessage("ChunkLoadEvent");
		Chunk chunk = event.getChunk();
		Map<Location, List<ItemStack>> itemMap = CraftInventoryLoader.loadChunk(Main.plugin.getSaveFolder(), chunk.getWorld(), chunk.getX(), chunk.getZ());
		for (Location location : itemMap.keySet()) {
			Main.manager.load(location, itemMap.get(location));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChunkUnloadEvent(ChunkUnloadEvent event) {
		//Utils.sendMessage("ChunkUnloadEvent -> " + new Boolean(event.getChunk().isLoaded()).toString());
		//if (event.getChunk().isLoaded()) {
			//return;
		//}
		//Utils.sendMessage("ChunkUnloadEvent -> " + new Integer(event.getChunk().getX()).toString() + " " + new Integer(event.getChunk().getZ()).toString());
		//Utils.sendMessage("ChunkUnloadEvent -> " + new Boolean(Main.manager.unload(event.getChunk())).toString());
		Main.manager.unload(event.getChunk());
	}
}
