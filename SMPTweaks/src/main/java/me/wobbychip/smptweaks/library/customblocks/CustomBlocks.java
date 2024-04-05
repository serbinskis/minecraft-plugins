package me.wobbychip.smptweaks.library.customblocks;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomMarker;
import me.wobbychip.smptweaks.library.customblocks.events.BlockEvents;
import me.wobbychip.smptweaks.library.customblocks.events.InventoryEvents;
import me.wobbychip.smptweaks.library.customblocks.events.WorldEvents;
import me.wobbychip.smptweaks.library.customblocks.test.TestBlock;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.ItemDisplay;

import java.util.HashMap;
import java.util.UUID;

public class CustomBlocks {
	public static final String RESOURCE_PACK_PROMPT = "This resource pack is required for custom blocks.";
	public static final String RESOURCE_PACK_URL = "https://github.com/WobbyChip/Minecraft-Plugins/raw/master/SMPTweaks/src/main/resources/resourcepack.zip";
	public static final byte[] RESOURCE_PACK_HASH = Utils.getFileHash(RESOURCE_PACK_URL);
	public static final UUID RESOURCE_PACK_UUID = UUID.nameUUIDFromBytes(RESOURCE_PACK_HASH);
	public static HashMap<String, CustomBlock> REGISTRY_CUSTOM_BLOCKS = new HashMap<>();

	public static void start() {
		Bukkit.getPluginManager().registerEvents(new WorldEvents(), Main.plugin);
		if (Main.DEBUG_MODE) { CustomBlocks.registerBlock(new TestBlock()); }
		CustomMarker.collectUnmarkedBlocks();
	}

	public static void registerBlock(CustomBlock block) {
		REGISTRY_CUSTOM_BLOCKS.put(block.getId().toLowerCase(), block);
		Bukkit.getPluginManager().registerEvents(block, Main.plugin);
		Bukkit.getPluginManager().registerEvents(new BlockEvents(block), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(block), Main.plugin);
		if (block.getRecipe() != null) { Bukkit.addRecipe(block.getRecipe()); }
	}

	public static int getSize() {
		return REGISTRY_CUSTOM_BLOCKS.size();
	}

	public static CustomBlock getCustomBlock(ItemDisplay display) {
		String name = PersistentUtils.getPersistentDataString(display, CustomBlock.TAG_BLOCK);
		return getCustomBlock(name);
	}

	public static CustomBlock getCustomBlock(String name) {
		return REGISTRY_CUSTOM_BLOCKS.getOrDefault(name.toLowerCase(), null);
	}
}
