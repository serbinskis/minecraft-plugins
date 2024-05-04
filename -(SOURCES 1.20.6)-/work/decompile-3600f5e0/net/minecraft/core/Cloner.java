package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;

public class Cloner<T> {

    private final Codec<T> directCodec;

    Cloner(Codec<T> codec) {
        this.directCodec = codec;
    }

    public T clone(T t0, HolderLookup.a holderlookup_a, HolderLookup.a holderlookup_a1) {
        DynamicOps<Object> dynamicops = holderlookup_a.createSerializationContext(JavaOps.INSTANCE);
        DynamicOps<Object> dynamicops1 = holderlookup_a1.createSerializationContext(JavaOps.INSTANCE);
        Object object = this.directCodec.encodeStart(dynamicops, t0).getOrThrow((s) -> {
            return new IllegalStateException("Failed to encode: " + s);
        });

        return this.directCodec.parse(dynamicops1, object).getOrThrow((s) -> {
            return new IllegalStateException("Failed to decode: " + s);
        });
    }

    public static class a {

        private final Map<ResourceKey<? extends IRegistry<?>>, Cloner<?>> codecs = new HashMap();

        public a() {}

        public <T> Cloner.a addCodec(ResourceKey<? extends IRegistry<? extends T>> resourcekey, Codec<T> codec) {
            this.codecs.put(resourcekey, new Cloner<>(codec));
            return this;
        }

        @Nullable
        public <T> Cloner<T> cloner(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
            return (Cloner) this.codecs.get(resourcekey);
        }
    }
}
