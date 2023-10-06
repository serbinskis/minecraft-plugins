package net.minecraft.server.packs.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.SystemUtils;

public interface MetadataSectionType<T> extends ResourcePackMetaParser<T> {

    JsonObject toJson(T t0);

    static <T> MetadataSectionType<T> fromCodec(final String s, final Codec<T> codec) {
        return new MetadataSectionType<T>() {
            @Override
            public String getMetadataSectionName() {
                return s;
            }

            @Override
            public T fromJson(JsonObject jsonobject) {
                return SystemUtils.getOrThrow(codec.parse(JsonOps.INSTANCE, jsonobject), JsonParseException::new);
            }

            @Override
            public JsonObject toJson(T t0) {
                return ((JsonElement) SystemUtils.getOrThrow(codec.encodeStart(JsonOps.INSTANCE, t0), IllegalArgumentException::new)).getAsJsonObject();
            }
        };
    }
}
