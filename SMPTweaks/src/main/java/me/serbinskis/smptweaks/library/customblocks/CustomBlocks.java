package me.serbinskis.smptweaks.library.customblocks;

import me.serbinskis.smptweaks.library.customblocks.blocks.CustomMarker;
import me.serbinskis.smptweaks.library.customblocks.commands.Commands;
import me.serbinskis.smptweaks.library.customblocks.custom.PlayerResetBlock;
import me.serbinskis.smptweaks.library.customblocks.events.BlockEvents;
import me.serbinskis.smptweaks.library.customblocks.events.InventoryEvents;
import me.serbinskis.smptweaks.library.customblocks.events.WorldEvents;
import me.serbinskis.smptweaks.library.customblocks.test.MovableBlock;
import me.serbinskis.smptweaks.library.customblocks.test.TestBlock;
import me.serbinskis.smptweaks.library.customblocks.textures.TextureGenerator;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class CustomBlocks extends CustomTweak {
	public static final String RESOURCE_PACK_PROMPT = "This resource pack is required for custom blocks.";
	public static String RESOURCE_PACK_URL;
	public static String RESOURCE_PACK_CDN_URL;
	public static byte[] RESOURCE_PACK_HASH;
	public static UUID RESOURCE_PACK_UUID;
	public static HashMap<String, CustomBlock> REGISTRY_CUSTOM_BLOCKS = new HashMap<>();
	public static ShapedRecipe EMPTY_RECIPE = new ShapedRecipe(new NamespacedKey(Main.plugin, "f9300cc0c1434e088d114b8869dd379e"), new ItemStack(Material.POISONOUS_POTATO));

	public CustomBlocks() {
		super(CustomBlocks.class, false, false, true);
		this.setCommand(new Commands(this, "cblocks"));
		this.setDescription("Library for custom blocks");
	}

	public static void start() {
		EMPTY_RECIPE.shape("AAA", "AAA", "AAA");
		ItemStack itemStack = PersistentUtils.setPersistentDataString(new ItemStack(Material.STRUCTURE_VOID), Utils.randomString(16, false), Utils.randomString(16, false));
		EMPTY_RECIPE.setIngredient('A', new RecipeChoice.ExactChoice(itemStack)); //Generate item that 100% nobody can have.

		Bukkit.getPluginManager().registerEvents(new WorldEvents(), Main.plugin);
		if (Main.DEBUG_MODE) { CustomBlocks.registerBlock(new TestBlock()); }
		if (Main.DEBUG_MODE) { CustomBlocks.registerBlock(new MovableBlock()); }
		CustomBlocks.registerBlock(new PlayerResetBlock());

		for (CustomBlock customBlock : REGISTRY_CUSTOM_BLOCKS.values()) {
			Bukkit.getPluginManager().registerEvents(customBlock, Main.plugin);
			Bukkit.getPluginManager().registerEvents(new BlockEvents(customBlock), Main.plugin);
			Bukkit.getPluginManager().registerEvents(new InventoryEvents(customBlock), Main.plugin);
			if (customBlock.getRecipe() != null) { Bukkit.addRecipe(customBlock.getRecipe()); }
		}

		updateTextures(REGISTRY_CUSTOM_BLOCKS.values());
		TaskUtils.scheduleAsyncRepeatingTask(() -> CustomBlocks.updateTextures(REGISTRY_CUSTOM_BLOCKS.values()), 20L*60*60*24, 20L*60*60*24);
		TaskUtils.scheduleAsyncRepeatingTask(CustomBlocks::updateCdnRedirect, 20L*30, 20L*30);
		CustomMarker.collectUnmarkedBlocks();
	}

	private static void updateTextures(Collection<CustomBlock> customBlocks) {
		TextureGenerator generator = new TextureGenerator(customBlocks);
		RESOURCE_PACK_URL = generator.upload();

		if (RESOURCE_PACK_URL == null) {
			TaskUtils.scheduleSyncDelayedTask(() -> CustomBlocks.updateTextures(customBlocks), 20L*60*5);
			Utils.sendMessage("[SMPTweaks] Failed to upload custom resource pack to filebin.net");
			updateCdnRedirect();
			return;
		} else {
			Utils.sendMessage("[SMPTweaks] Successfully uploaded custom resource pack to: " + RESOURCE_PACK_URL);
		}

		RESOURCE_PACK_HASH = Utils.getFileHash(generator.generate());
		RESOURCE_PACK_UUID = UUID.nameUUIDFromBytes(RESOURCE_PACK_HASH);
		updateCdnRedirect();
	}

	private static void updateCdnRedirect() {
		RESOURCE_PACK_CDN_URL = null;
		if (RESOURCE_PACK_URL == null) { return; }

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(RESOURCE_PACK_URL).openConnection();
			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);
			String verified = connection.getHeaderFields().get("Set-Cookie").getFirst();
			connection.disconnect();

			connection = (HttpURLConnection) new URL(RESOURCE_PACK_URL).openConnection();
			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);
			connection.setRequestProperty("Cookie", verified);

			if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
				RESOURCE_PACK_CDN_URL = connection.getHeaderField("Location");
			}

			connection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void registerBlock(CustomBlock customBlock) {
		REGISTRY_CUSTOM_BLOCKS.put(customBlock.getId().toLowerCase(), customBlock);
	}

	public static int getSize() {
		return REGISTRY_CUSTOM_BLOCKS.size();
	}

	public static CustomBlock getCustomBlock(ItemDisplay display) {
		String name = PersistentUtils.getPersistentDataString(display, CustomBlock.TAG_BLOCK);
		return getCustomBlock(name);
	}

	public static CustomBlock getCustomBlock(Block block) {
		CustomMarker marker = CustomMarker.getMarker(block);
		return (marker != null) ? marker.getCustomBlock() : null;
	}

	public static CustomBlock getCustomBlock(String name) {
		return REGISTRY_CUSTOM_BLOCKS.getOrDefault(name.toLowerCase(), null);
	}

	public static boolean isCustomBlock(Block block) {
		CustomMarker marker = CustomMarker.getMarker(block);
		return (marker != null);
	}
}
