package me.serbinskis.smptweaks.library.customblocks.textures;

import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import me.serbinskis.smptweaks.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TextureGenerator {
    public static int PACK_FORMAT = 46;
    public static String PACK_DESCRIPTION = "SMPTweaks resource pack for custom blocks.";
    public static String PACK_MCMETA = "{\"pack\":{ \"pack_format\": " + PACK_FORMAT + ", \"description\": \"" + PACK_DESCRIPTION + "\" }}";
    public static String PACK_IMAGE = "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";

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
    private byte[] texture_pack_data = null;

    public TextureGenerator(Collection<CustomBlock> customBlocks) {
        zipOutputStream.setLevel(9);
        addZipEntry("pack.mcmeta", PACK_MCMETA.getBytes());
        addZipEntry("pack.png", Base64.getDecoder().decode(PACK_IMAGE));
        addCustomBlocks(customBlocks);
    }

    public void addCustomBlock(CustomBlock customBlock) {
        String block_base = customBlock.getBlockBase().toString().toLowerCase();
        byte[][] textures = customBlock.getCustomTextures();
        if (textures == null) { return; }

        addBlockTexture(block_base, customBlock.getId() + "_0", textures[0]);
        addBlockTexture(block_base, customBlock.getId() + "_1", textures[1]);
    }

    public void addCustomBlocks(Collection<CustomBlock> customBlocks) {
        customBlocks.forEach(this::addCustomBlock);
    }

    private void addBlockTexture(String block_base, String cid, byte[] texture) {
        String block_json = "{\n" +
                "\t\"parent\": \"block/cube\",\n" +
                "\t\"textures\": {\n" +
                "        \"up\": \"smptweaks:block/" + cid + "/up\",\n" +
                "        \"down\": \"smptweaks:block/" + cid + "/down\",\n" +
                "        \"east\": \"smptweaks:block/" + cid + "/east\",\n" +
                "        \"north\": \"smptweaks:block/" + cid + "/north\",\n" +
                "        \"south\": \"smptweaks:block/" + cid + "/south\",\n" +
                "        \"west\": \"smptweaks:block/" + cid + "/west\",\n" +
                "        \"particle\": \"smptweaks:block/" + cid + "/particle\"\n" +
                "\t}\n" +
                "}";

        String block_model = "{\n" +
                "  \"model\": {\n" +
                "    \"type\": \"model\",\n" +
                "    \"model\": \"smptweaks:item/blocks/" + cid + "\"\n" +
                "  }\n" +
                "}";

        TextureSplitter.BlockTexture blockTexture = TextureSplitter.splitTexture(texture);
        addZipEntry("assets/smptweaks/textures/block/" + cid + "/up.png", blockTexture.up());
        addZipEntry("assets/smptweaks/textures/block/" + cid + "/down.png", blockTexture.down());
        addZipEntry("assets/smptweaks/textures/block/" + cid + "/east.png", blockTexture.east());
        addZipEntry("assets/smptweaks/textures/block/" + cid + "/north.png", blockTexture.north());
        addZipEntry("assets/smptweaks/textures/block/" + cid + "/south.png", blockTexture.south());
        addZipEntry("assets/smptweaks/textures/block/" + cid + "/west.png", blockTexture.west());
        addZipEntry("assets/smptweaks/textures/block/" + cid + "/particle.png", blockTexture.particle());
        addZipEntry("assets/smptweaks/models/item/blocks/" + cid + ".json", block_json.getBytes());
        addZipEntry("assets/smptweaks/items/blocks/" + cid + ".json", block_model.getBytes());
    }

    private void addZipEntry(String entryName, byte[] buffer) {
        try {
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            if (buffer.length > 0) { zipOutputStream.write(buffer); }
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generate() {
        if (texture_pack_data != null) { return texture_pack_data; }

        try {
            zipOutputStream.finish();
            zipOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        texture_pack_data  = byteArrayOutputStream.toByteArray();
        return texture_pack_data;
    }

    public String upload() {
        String url = "https://filebin.net/" + Utils.randomString(56, false) + "/resourcepack.zip";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(this.generate()))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 201) ? url : null;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
