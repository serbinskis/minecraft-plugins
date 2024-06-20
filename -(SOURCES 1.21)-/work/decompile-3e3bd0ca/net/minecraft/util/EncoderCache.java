package net.minecraft.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NBTBase;

public class EncoderCache {

    final LoadingCache<EncoderCache.a<?, ?>, DataResult<?>> cache;

    public EncoderCache(int i) {
        this.cache = CacheBuilder.newBuilder().maximumSize((long) i).concurrencyLevel(1).softValues().build(new CacheLoader<EncoderCache.a<?, ?>, DataResult<?>>(this) {
            public DataResult<?> load(EncoderCache.a<?, ?> encodercache_a) {
                return encodercache_a.resolve();
            }
        });
    }

    public <A> Codec<A> wrap(final Codec<A> codec) {
        return new Codec<A>() {
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicops, T t0) {
                return codec.decode(dynamicops, t0);
            }

            public <T> DataResult<T> encode(A a0, DynamicOps<T> dynamicops, T t0) {
                return ((DataResult) EncoderCache.this.cache.getUnchecked(new EncoderCache.a<>(codec, a0, dynamicops))).map((object) -> {
                    if (object instanceof NBTBase nbtbase) {
                        return nbtbase.copy();
                    } else {
                        return object;
                    }
                });
            }
        };
    }

    private static record a<A, T>(Codec<A> codec, A value, DynamicOps<T> ops) {

        public DataResult<T> resolve() {
            return this.codec.encodeStart(this.ops, this.value);
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (!(object instanceof EncoderCache.a)) {
                return false;
            } else {
                EncoderCache.a<?, ?> encodercache_a = (EncoderCache.a) object;

                return this.codec == encodercache_a.codec && this.value.equals(encodercache_a.value) && this.ops.equals(encodercache_a.ops);
            }
        }

        public int hashCode() {
            int i = System.identityHashCode(this.codec);

            i = 31 * i + this.value.hashCode();
            i = 31 * i + this.ops.hashCode();
            return i;
        }
    }
}
