package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface DataComponentType<T> {

    Codec<DataComponentType<?>> CODEC = Codec.lazyInitialized(() -> {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec();
    });
    StreamCodec<RegistryFriendlyByteBuf, DataComponentType<?>> STREAM_CODEC = StreamCodec.recursive((streamcodec) -> {
        return ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE);
    });
    Codec<DataComponentType<?>> PERSISTENT_CODEC = DataComponentType.CODEC.validate((datacomponenttype) -> {
        return datacomponenttype.isTransient() ? DataResult.error(() -> {
            return "Encountered transient component " + String.valueOf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(datacomponenttype));
        }) : DataResult.success(datacomponenttype);
    });
    Codec<Map<DataComponentType<?>, Object>> VALUE_MAP_CODEC = Codec.dispatchedMap(DataComponentType.PERSISTENT_CODEC, DataComponentType::codecOrThrow);

    static <T> DataComponentType.a<T> builder() {
        return new DataComponentType.a<>();
    }

    @Nullable
    Codec<T> codec();

    default Codec<T> codecOrThrow() {
        Codec<T> codec = this.codec();

        if (codec == null) {
            throw new IllegalStateException(String.valueOf(this) + " is not a persistent component");
        } else {
            return codec;
        }
    }

    default boolean isTransient() {
        return this.codec() == null;
    }

    StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

    public static class a<T> {

        @Nullable
        private Codec<T> codec;
        @Nullable
        private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
        private boolean cacheEncoding;

        public a() {}

        public DataComponentType.a<T> persistent(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public DataComponentType.a<T> networkSynchronized(StreamCodec<? super RegistryFriendlyByteBuf, T> streamcodec) {
            this.streamCodec = streamcodec;
            return this;
        }

        public DataComponentType.a<T> cacheEncoding() {
            this.cacheEncoding = true;
            return this;
        }

        public DataComponentType<T> build() {
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamcodec = (StreamCodec) Objects.requireNonNullElseGet(this.streamCodec, () -> {
                return ByteBufCodecs.fromCodecWithRegistries((Codec) Objects.requireNonNull(this.codec, "Missing Codec for component"));
            });
            Codec<T> codec = this.cacheEncoding && this.codec != null ? DataComponents.ENCODER_CACHE.wrap(this.codec) : this.codec;

            return new DataComponentType.a.a<>(codec, streamcodec);
        }

        private static class a<T> implements DataComponentType<T> {

            @Nullable
            private final Codec<T> codec;
            private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

            a(@Nullable Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamcodec) {
                this.codec = codec;
                this.streamCodec = streamcodec;
            }

            @Nullable
            @Override
            public Codec<T> codec() {
                return this.codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return this.streamCodec;
            }

            public String toString() {
                return SystemUtils.getRegisteredName(BuiltInRegistries.DATA_COMPONENT_TYPE, this);
            }
        }
    }
}
