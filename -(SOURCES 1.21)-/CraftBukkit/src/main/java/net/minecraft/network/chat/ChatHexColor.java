package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;

public final class ChatHexColor {

    private static final String CUSTOM_COLOR_PREFIX = "#";
    public static final Codec<ChatHexColor> CODEC = Codec.STRING.comapFlatMap(ChatHexColor::parseColor, ChatHexColor::serialize);
    private static final Map<EnumChatFormat, ChatHexColor> LEGACY_FORMAT_TO_COLOR = (Map) Stream.of(EnumChatFormat.values()).filter(EnumChatFormat::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), (enumchatformat) -> {
        return new ChatHexColor(enumchatformat.getColor(), enumchatformat.getName(), enumchatformat); // CraftBukkit
    }));
    private static final Map<String, ChatHexColor> NAMED_COLORS = (Map) ChatHexColor.LEGACY_FORMAT_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap((chathexcolor) -> {
        return chathexcolor.name;
    }, Function.identity()));
    private final int value;
    @Nullable
    public final String name;
    // CraftBukkit start
    @Nullable
    public final EnumChatFormat format;

    private ChatHexColor(int i, String s, EnumChatFormat format) {
        this.value = i & 16777215;
        this.name = s;
        this.format = format;
    }

    private ChatHexColor(int i) {
        this.value = i & 16777215;
        this.name = null;
        this.format = null;
    }
    // CraftBukkit end

    public int getValue() {
        return this.value;
    }

    public String serialize() {
        return this.name != null ? this.name : this.formatValue();
    }

    private String formatValue() {
        return String.format(Locale.ROOT, "#%06X", this.value);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            ChatHexColor chathexcolor = (ChatHexColor) object;

            return this.value == chathexcolor.value;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.value, this.name});
    }

    public String toString() {
        return this.serialize();
    }

    @Nullable
    public static ChatHexColor fromLegacyFormat(EnumChatFormat enumchatformat) {
        return (ChatHexColor) ChatHexColor.LEGACY_FORMAT_TO_COLOR.get(enumchatformat);
    }

    public static ChatHexColor fromRgb(int i) {
        return new ChatHexColor(i);
    }

    public static DataResult<ChatHexColor> parseColor(String s) {
        if (s.startsWith("#")) {
            try {
                int i = Integer.parseInt(s.substring(1), 16);

                return i >= 0 && i <= 16777215 ? DataResult.success(fromRgb(i), Lifecycle.stable()) : DataResult.error(() -> {
                    return "Color value out of range: " + s;
                });
            } catch (NumberFormatException numberformatexception) {
                return DataResult.error(() -> {
                    return "Invalid color value: " + s;
                });
            }
        } else {
            ChatHexColor chathexcolor = (ChatHexColor) ChatHexColor.NAMED_COLORS.get(s);

            return chathexcolor == null ? DataResult.error(() -> {
                return "Invalid color name: " + s;
            }) : DataResult.success(chathexcolor, Lifecycle.stable());
        }
    }
}
