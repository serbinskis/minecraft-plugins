package me.serbinskis.smptweaks.library.customtextures;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

public class CustomTextures {
    public static TextureGenerator RESOURCE_PACK = new TextureGenerator();
    public static String RESOURCE_PACK_URL;
    public static byte[] RESOURCE_PACK_HASH;
    public static UUID RESOURCE_PACK_UUID;
    public static final String RESOURCE_PACK_PROMPT = "This resource pack is required for custom blocks.";
    private static int itemCount = 0;

    public static void start() {
        RESOURCE_PACK_HASH = Utils.getFileHash(RESOURCE_PACK.generate());
        RESOURCE_PACK_UUID = UUID.nameUUIDFromBytes(RESOURCE_PACK_HASH);
        Bukkit.getPluginManager().registerEvents(new TextureEvents(), Main.plugin);

        TaskUtils.scheduleAsyncRepeatingTask(CustomTextures::upload, 20L*60*60*24, 20L*60*60*24);
        CustomTextures.upload();
    }

    public static void upload() {
        TextureUploader.upload(RESOURCE_PACK, url -> RESOURCE_PACK_URL = url);
    }

    public static void addCustomBlocks(Collection<CustomBlock> customBlocks) {
        RESOURCE_PACK.addCustomBlocks(customBlocks);
        itemCount += customBlocks.size();
    }

    public static void addCustomItems(Collection<CustomItem> customItems) {
        RESOURCE_PACK.addCustomItems(customItems);
        itemCount += customItems.size();
    }

    public static int getSize() {
        return itemCount;
    }
}
