package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public record ColorRGBA(int rgba) {

    private static final String CUSTOM_COLOR_PREFIX = "#";
    public static final Codec<ColorRGBA> CODEC = Codec.STRING.comapFlatMap((s) -> {
        if (!s.startsWith("#")) {
            return DataResult.error(() -> {
                return "Not a color code: " + s;
            });
        } else {
            try {
                int i = (int) Long.parseLong(s.substring(1), 16);

                return DataResult.success(new ColorRGBA(i));
            } catch (NumberFormatException numberformatexception) {
                return DataResult.error(() -> {
                    return "Exception parsing color code: " + numberformatexception.getMessage();
                });
            }
        }
    }, ColorRGBA::formatValue);

    private String formatValue() {
        return String.format(Locale.ROOT, "#%08X", this.rgba);
    }

    public String toString() {
        return this.formatValue();
    }
}
