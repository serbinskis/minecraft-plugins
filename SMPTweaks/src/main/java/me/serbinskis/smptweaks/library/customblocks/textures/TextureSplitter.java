package me.serbinskis.smptweaks.library.customblocks.textures;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextureSplitter {
    public static BlockTexture splitTexture(byte[] texture) {
        try {
            final BufferedImage texture_image = ImageIO.read(new ByteArrayInputStream(texture));
            int partial_width = texture_image.getWidth() / 7;
            byte[] up = imageToByteArray(texture_image.getSubimage(0, 0, partial_width, partial_width));
            byte[] down = imageToByteArray(texture_image.getSubimage(partial_width, 0, partial_width, partial_width));
            byte[] east = imageToByteArray(texture_image.getSubimage(partial_width * 2, 0, partial_width, partial_width));
            byte[] west = imageToByteArray(texture_image.getSubimage(partial_width * 3, 0, partial_width, partial_width));
            byte[] north = imageToByteArray(texture_image.getSubimage(partial_width * 4, 0, partial_width, partial_width));
            byte[] south = imageToByteArray(texture_image.getSubimage(partial_width * 5, 0, partial_width, partial_width));
            byte[] particle = imageToByteArray(texture_image.getSubimage(partial_width * 6, 0, partial_width, partial_width));
            return new BlockTexture(up, down, east, west, north, south, particle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[][] splitTexture(InputStream stream) {
        try {
            final BufferedImage texture_image = ImageIO.read(stream);
            int partial_height = texture_image.getHeight() / 2;
            byte[] model = imageToByteArray(texture_image.getSubimage(0, 0, texture_image.getWidth(), partial_height));
            byte[] model_extra = imageToByteArray(texture_image.getSubimage(0, partial_height, texture_image.getWidth(), partial_height));
            return new byte[][] { model, model_extra };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] imageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public record BlockTexture(byte[] up, byte[] down, byte[] east, byte[] west, byte[] north, byte[] south, byte[] particle) {}
}
