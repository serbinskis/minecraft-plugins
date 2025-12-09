package me.serbinskis.smptweaks.library.customtextures;

import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class TextureUploader {
    private static String RESOURCE_PACK_URL = null;
    private static final long RETRY_TIME_INTERVAL = 20L*60*5;
    private static long lastUploadTime = 0;
    private static long lastUpdateCdnTime = 0;
    private static int retryTimer = -1;

    public static void upload(TextureGenerator generator, Consumer<String> setter) {
        if (retryTimer > -1) { return; }

        TextureUploader.uploadFileBin(generator, setter);
        if (RESOURCE_PACK_URL == null) { TextureUploader.uploadAlternative(generator, setter); }

        if (RESOURCE_PACK_URL != null) { lastUploadTime = System.currentTimeMillis(); return; }
        retryTimer = TaskUtils.scheduleAsyncDelayedTask(() -> { retryTimer = -1; TextureUploader.upload(generator, setter); }, RETRY_TIME_INTERVAL);
    }

    private static void uploadAlternative(TextureGenerator generator, Consumer<String> setter) {
        //TODO: Make alternative
    }

    private static void uploadFileBin(TextureGenerator generator, Consumer<String> setter) {
        if (lastUpdateCdnTime > 0) { updateCdnRedirect(setter); }
        if (System.currentTimeMillis() - lastUploadTime < 1000*60*60*23) { return; }
        String url = "https://filebin.net/" + Utils.randomString(16, false) + "/" + Utils.randomString(8, false) + ".zip";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(generator.generate()))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            RESOURCE_PACK_URL = (response.statusCode() == 201) ? url : null;
        } catch (IOException | InterruptedException e) {
            RESOURCE_PACK_URL = null;
        }

        if (RESOURCE_PACK_URL == null) {
            Utils.sendMessage("[SMPTweaks] Failed to upload custom resource pack to filebin.net");
            return;
        }

        updateCdnRedirect(setter);
        Utils.sendMessage("[SMPTweaks] Successfully uploaded custom resource pack to: " + url);
    }

    private static void updateCdnRedirect(Consumer<String> setter) {
        if (System.currentTimeMillis() - lastUpdateCdnTime < 30000) { return; }
        if (RESOURCE_PACK_URL == null) { return; }
        lastUpdateCdnTime = System.currentTimeMillis(); //This prevents concurrent execution from AsyncPlayerPreLoginEvent

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
                setter.accept(connection.getHeaderField("Location"));
            }

            connection.disconnect();
        } catch (IOException e) {
            lastUpdateCdnTime = 1;
            e.printStackTrace();
        }
    }
}
