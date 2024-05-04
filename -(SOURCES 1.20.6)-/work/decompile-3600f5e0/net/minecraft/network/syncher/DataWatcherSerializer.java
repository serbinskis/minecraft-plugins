package net.minecraft.network.syncher;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface DataWatcherSerializer<T> {

    StreamCodec<? super RegistryFriendlyByteBuf, T> codec();

    default DataWatcherObject<T> createAccessor(int i) {
        return new DataWatcherObject<>(i, this);
    }

    T copy(T t0);

    static <T> DataWatcherSerializer<T> forValueType(StreamCodec<? super RegistryFriendlyByteBuf, T> streamcodec) {
        return () -> {
            return streamcodec;
        };
    }

    public interface a<T> extends DataWatcherSerializer<T> {

        @Override
        default T copy(T t0) {
            return t0;
        }
    }
}
