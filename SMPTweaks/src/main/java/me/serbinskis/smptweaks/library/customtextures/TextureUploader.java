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
        if (RESOURCE_PACK_URL == null) { TextureUploader.uploadLitterbox(generator, setter); }
        if (RESOURCE_PACK_URL == null) { TextureUploader.uploadCatbox(generator, setter); }

        if (RESOURCE_PACK_URL != null) { lastUploadTime = System.currentTimeMillis(); return; }
        retryTimer = TaskUtils.scheduleAsyncDelayedTask(() -> { retryTimer = -1; TextureUploader.upload(generator, setter); }, RETRY_TIME_INTERVAL);
    }

    private static void uploadLitterbox(TextureGenerator generator, Consumer<String> setter) {
        if (System.currentTimeMillis() - lastUploadTime < 1000 * 60 * 60 * 23) { return; }

        String boundary = "----SMPTweaksBoundary" + System.currentTimeMillis();
        String fieldReq = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"reqtype\"\r\n\r\n" + "fileupload\r\n";
        String fieldTime = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"time\"\r\n\r\n" + "72h\r\n";
        String fieldFile = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"" + Utils.randomString(8, false) + "\"\r\n" + "Content-Type: application/octet-stream\r\n\r\n";
        String footer = "\r\n--" + boundary + "--\r\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try { outputStream.write(fieldReq.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}
        try { outputStream.write(fieldTime.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}
        try { outputStream.write(fieldFile.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}
        try { outputStream.write(generator.generate()); } catch (Exception ignored) {}
        try { outputStream.write(footer.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://litterbox.catbox.moe/resources/internals/api.php"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("User-Agent", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofByteArray(outputStream.toByteArray()))
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
        String boundary = "----SMPTweaksBoundary" + System.currentTimeMillis();
        String footer = "\r\n--" + boundary + "--\r\n";

        String reqType = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"reqtype\"\r\n\r\n" +
                "fileupload\r\n";

        String fileHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"" + Utils.randomString(8, false) + ".zip\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try { outputStream.write(reqType.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}
        try { outputStream.write(fileHeader.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}
        try { outputStream.write(generator.generate()); } catch (Exception ignored) {}
        try { outputStream.write(footer.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://catbox.moe/user/api.php"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("User-Agent", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofByteArray(outputStream.toByteArray()))
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

        String boundary = "----SMPTweaksBoundary" + System.currentTimeMillis();
        String footer = "\r\n--" + boundary + "--\r\n";
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + Utils.randomString(8, false) + ".zip\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try { outputStream.write(header.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}
        try { outputStream.write(generator.generate()); } catch (Exception ignored) {}
        try { outputStream.write(footer.getBytes(StandardCharsets.UTF_8)); } catch (Exception ignored) {}

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://0x0.st"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("User-Agent", "SMPTweaks-Plugin/" + Main.getPlugin().getDescription().getVersion())
                .POST(HttpRequest.BodyPublishers.ofByteArray(outputStream.toByteArray()))
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
        byte[] fileData = generator.generate();
        String boundary = "----SMPTweaksBoundary" + System.currentTimeMillis();
        String bodySuffix = "\r\n--" + boundary + "--\r\n";

        String bodyPrefix = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + Utils.randomString(8, false) + ".zip\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";

        byte[] body = new byte[bodyPrefix.getBytes().length + fileData.length + bodySuffix.getBytes().length];
        System.arraycopy(bodyPrefix.getBytes(), 0, body, 0, bodyPrefix.getBytes().length);
        System.arraycopy(fileData, 0, body, bodyPrefix.getBytes().length, fileData.length);
        System.arraycopy(bodySuffix.getBytes(), 0, body, bodyPrefix.getBytes().length + fileData.length, bodySuffix.getBytes().length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://upload.gofile.io/uploadFile"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
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
}
