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
    public static String RESOURCE_PACK_CDN_URL;
    public static byte[] RESOURCE_PACK_HASH;
    public static UUID RESOURCE_PACK_UUID;
    public static final String RESOURCE_PACK_PROMPT = "This resource pack is required for custom blocks.";
    private static long lastUpdateCdnTime = 0;
    private static int itemCount = 0;

    public static void start() {
        TaskUtils.scheduleAsyncRepeatingTask(CustomTextures::upload, 20L*60*60*24, 20L*60*60*24);
        CustomTextures.upload();
    }

    public static void upload() {
        RESOURCE_PACK_URL = RESOURCE_PACK.upload();

        if (RESOURCE_PACK_URL == null) {
            TaskUtils.scheduleSyncDelayedTask(CustomTextures::upload, 20L*60*5);
            Utils.sendMessage("[SMPTweaks] Failed to upload custom resource pack to filebin.net");
            updateCdnRedirect();
            return;
        } else {
            Utils.sendMessage("[SMPTweaks] Successfully uploaded custom resource pack to: " + RESOURCE_PACK_URL);
        }

        Bukkit.getPluginManager().registerEvents(new TextureEvents(), Main.plugin);
        RESOURCE_PACK_HASH = Utils.getFileHash(RESOURCE_PACK.generate());
        RESOURCE_PACK_UUID = UUID.nameUUIDFromBytes(RESOURCE_PACK_HASH);
        updateCdnRedirect();
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

    public static void updateCdnRedirect() {
        if (System.currentTimeMillis() - lastUpdateCdnTime < 30000) { return; }
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
                lastUpdateCdnTime = System.currentTimeMillis();
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
