package me.wobbychip.smptweaks.library.customblocks;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomMarker;
import me.wobbychip.smptweaks.library.customblocks.events.BlockEvents;
import me.wobbychip.smptweaks.library.customblocks.events.InventoryEvents;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;

import java.util.HashMap;
import java.util.Map;

import static me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock.BLOCK_TAG;

public class CustomBlocks {
	public static HashMap<String, CustomBlock> cblocks = new HashMap<>();

	public static void start() {
		//Since block are marked with entities and entities can unload
		//We collect them every tick and recreate markers

		TaskUtils.scheduleSyncRepeatingTask(new Runnable() {
			public void run() {
				for (Map.Entry<Block, BlockDisplay> value : CustomMarker.collectUnmarkedBlocks().entrySet()) {
					value.getValue().remove();
					CustomBlock customBlock = getCustomBlock(value.getValue());
					if (customBlock != null) { customBlock.createBlock(value.getKey()); }
				}
			}
		}, 1L, 1L);
	}

	public static void registerBlock(CustomBlock block) {
		cblocks.put(block.getName().toLowerCase(), block);
		Bukkit.getPluginManager().registerEvents(block, Main.plugin);
		Bukkit.getPluginManager().registerEvents(new BlockEvents(block), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(block), Main.plugin);
		if (block.getRecipe() != null) { Bukkit.addRecipe(block.getRecipe()); }
	}

	public static CustomBlock getCustomBlock(Block block) {
		CustomMarker marker = CustomMarker.getMarker(block);
		return (marker != null) ? getCustomBlock(marker.getName()) : null;
	}

	public static CustomBlock getCustomBlock(BlockDisplay display) {
		String name = PersistentUtils.getPersistentDataString(display, BLOCK_TAG);
		return getCustomBlock(name);
	}

	public static CustomBlock getCustomBlock(String name) {
		return cblocks.getOrDefault(name.toLowerCase(), null);
	}
}
