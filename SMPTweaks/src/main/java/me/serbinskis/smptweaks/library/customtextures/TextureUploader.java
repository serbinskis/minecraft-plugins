package me.serbinskis.smptweaks.library.customtextures;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class TextureUploader {
    private static String RESOURCE_PACK_URL = null;
    private static final long RETRY_TIME_INTERVAL = 20L*60*5;
    private static long lastUploadTime = 0;
    private static long lastUpdateCdnTime = 0;
    private static int retryTimer = -1;

    public static void upload(TextureGenerator generator, Consumer<String> setter) {
        if (retryTimer > -1) { return; }

        TextureUploader.uploadFileBin(generator, setter); //But this runs cdn update, so we must check time separately inside each of them
        if (RESOURCE_PACK_URL == null) { TextureUploader.uploadLitterbox(generator, setter); }
        if (RESOURCE_PACK_URL == null) { TextureUploader.uploadCatbox(generator, setter); }

        if (System.currentTimeMillis() - lastUploadTime < 1000 * 60 * 60 * 23) { return; } //We don't want to update lastUploadTime, every time on AsyncPlayerPreLoginEvent
        if (RESOURCE_PACK_URL != null) { lastUploadTime = System.currentTimeMillis(); return; }
        retryTimer = TaskUtils.scheduleAsyncDelayedTask(() -> { retryTimer = -1; TextureUploader.upload(generator, setter); }, RETRY_TIME_INTERVAL);
    }

    private static void uploadLitterbox(TextureGenerator generator, Consumer<String> setter) {
        if (System.currentTimeMillis() - lastUploadTime < 1000 * 60 * 60 * 23) { return; }
        Map<String, String> fields = Map.of("reqtype", "fileupload","time", "72h");
        Map.Entry<String, byte[]> payload = buildMultipartPayload(fields, "fileToUpload", null, generator.generate());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://litterbox.catbox.moe/resources/internals/api.php"))
                .header("Content-Type", "multipart/form-data; boundary=" + payload.getKey())
                .header("User-Agent", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload.getValue()))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if ((response.statusCode() == 200) && !response.body().isEmpty()) { RESOURCE_PACK_URL = response.body().trim(); }
        } catch (Exception e) { RESOURCE_PACK_URL = null; }

        if (RESOURCE_PACK_URL == null) {
            Utils.sendMessage("[SMPTweaks] Failed to upload resource pack litterbox.catbox.moe.");
            return;
        }

        Utils.sendMessage("[SMPTweaks] Successfully uploaded custom resource pack to: " + RESOURCE_PACK_URL);
        setter.accept(RESOURCE_PACK_URL);
    }

    private static void uploadCatbox(TextureGenerator generator, Consumer<String> setter) {
        if (System.currentTimeMillis() - lastUploadTime < 1000 * 60 * 60 * 23) { return; }
        Map<String, String> fields = Map.of("reqtype", "fileupload");
        Map.Entry<String, byte[]> payload = buildMultipartPayload(fields, "fileToUpload", null, generator.generate());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://catbox.moe/user/api.php"))
                .header("Content-Type", "multipart/form-data; boundary=" + payload.getKey())
                .header("User-Agent", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload.getValue()))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if ((response.statusCode() == 200) && !response.body().isEmpty()) { RESOURCE_PACK_URL = response.body().trim(); }
        } catch (Exception e) { RESOURCE_PACK_URL = null; }

        if (RESOURCE_PACK_URL == null) {
            Utils.sendMessage("[SMPTweaks] Failed to upload resource pack to catbox.moe.");
            return;
        }

        Utils.sendMessage("[SMPTweaks] Successfully uploaded custom resource pack to: " + RESOURCE_PACK_URL);
        setter.accept(RESOURCE_PACK_URL);
    }

    //FUCKING SHIT ASS DEV, BLOCKS JAVA USER AGENT, MEANING NO DOWNLOADS
    private static void upload0x0st(TextureGenerator generator, Consumer<String> setter) {
        if (System.currentTimeMillis() - lastUploadTime < 1000 * 60 * 60 * 23) { return; }
        Map.Entry<String, byte[]> payload = buildMultipartPayload(null, "file", null, generator.generate());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://0x0.st"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "multipart/form-data; boundary=" + payload.getKey())
                .header("User-Agent", "SMPTweaks-Plugin/" + Main.getPlugin().getDescription().getVersion())
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload.getValue()))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if ((response.statusCode() == 200) && !response.body().isEmpty()) { RESOURCE_PACK_URL = response.body().trim(); }
        } catch (Exception e) { RESOURCE_PACK_URL = null; }

        if (RESOURCE_PACK_URL == null) {
            Utils.sendMessage("[SMPTweaks] Failed to upload resource pack to 0x0.st.");
            return;
        }

        Utils.sendMessage("[SMPTweaks] Successfully uploaded custom resource pack to: " + RESOURCE_PACK_URL);
        setter.accept(RESOURCE_PACK_URL);
    }

    //FUCKING GARBAGE, REQUIRES TOKEN TO DOWNLOAD, WTF, SO UPLOADING IS FREE, BUT DOWNLOADING NO?
    private static void uploadGoFile(TextureGenerator generator, Consumer<String> setter) {
        if (System.currentTimeMillis() - lastUploadTime < 1000 * 60 * 60 * 23) { return; }
        Map.Entry<String, byte[]> payload = buildMultipartPayload(null, "file", null, generator.generate());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://upload.gofile.io/uploadFile"))
                .header("Content-Type", "multipart/form-data; boundary=" + payload.getKey())
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload.getValue()))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if ((response.statusCode() != 200) || !response.body().contains("\"status\":\"ok\"")) { throw new RuntimeException(); }
            String fileId = response.body().split("\"id\":\"")[1].split("\"")[0];
            String server = response.body().split("\"servers\":\\[\"")[1].split("\"")[0];
            String fileName = response.body().split("\"name\":\"")[1].split("\"")[0];
            RESOURCE_PACK_URL = "https://" + server + ".gofile.io/download/" + fileId + "/" + fileName;
        } catch (Exception e) { RESOURCE_PACK_URL = null; }

        if (RESOURCE_PACK_URL == null) {
            Utils.sendMessage("[SMPTweaks] Failed to upload resource pack to gofile.io.");
            return;
        }

        Utils.sendMessage("[SMPTweaks] Successfully uploaded custom resource pack to: " + RESOURCE_PACK_URL);
        setter.accept(RESOURCE_PACK_URL);
    }

    private static void uploadFileBin(TextureGenerator generator, Consumer<String> setter) {
        if (lastUpdateCdnTime > 0) { updateCdnRedirect(setter); }
        if (System.currentTimeMillis() - lastUploadTime < 1000 * 60 * 60 * 23) { return; }
        String url = "https://filebin.net/" + Utils.randomString(16, false) + "/" + Utils.randomString(8, false) + ".zip";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(generator.generate()))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            RESOURCE_PACK_URL = (response.statusCode() == 201) ? url : null;
        } catch (Exception e) { RESOURCE_PACK_URL = null; }

        if (RESOURCE_PACK_URL == null) {
            Utils.sendMessage("[SMPTweaks] Failed to upload custom resource pack to filebin.net");
            return;
        }

        updateCdnRedirect(setter);
        Utils.sendMessage("[SMPTweaks] Successfully uploaded custom resource pack to: " + RESOURCE_PACK_URL);
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

    /**
     * Builds a multipart/form-data payload with arbitrary fields and a single file.
     *
     * @param fields Map of form field names -> values (e.g., "reqtype" -> "fileupload")
     * @param fileFieldName Name of the file field (e.g., "fileToUpload")
     * @param fileName File name for upload. If null, a random 8-character file name will be generated.
     * @param fileData File contents as byte[]
     * @return a Map.Entry containing:
     *         - key: the generated multipart boundary string
     *         - value: the multipart payload as a byte array
     */
    private static Map.Entry<String, byte[]> buildMultipartPayload(Map<String, String> fields, String fileFieldName, String fileName, byte[] fileData) {
        String boundary = "----SMPTweaksBoundary" + System.currentTimeMillis();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (fileName == null) { fileName = Utils.randomString(8, false); }

        try {
            for (Map.Entry<?, ?> entry : Objects.requireNonNullElse(fields, Map.of()).entrySet()) {
                String fieldPart = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n";
                outputStream.write(fieldPart.getBytes(StandardCharsets.UTF_8));
            }

            String fileHeader = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + fileName + "\"\r\n" + "Content-Type: application/octet-stream\r\n\r\n";
            outputStream.write(fileHeader.getBytes(StandardCharsets.UTF_8));
            outputStream.write(fileData);
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build multipart payload", e);
        }

        return Map.entry(boundary, outputStream.toByteArray());
    }
}
