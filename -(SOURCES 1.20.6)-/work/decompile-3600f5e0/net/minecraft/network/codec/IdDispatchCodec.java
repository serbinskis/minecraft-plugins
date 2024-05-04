package net.minecraft.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.VarInt;

public class IdDispatchCodec<B extends ByteBuf, V, T> implements StreamCodec<B, V> {

    private static final int UNKNOWN_TYPE = -1;
    private final Function<V, ? extends T> typeGetter;
    private final List<IdDispatchCodec.b<B, V, T>> byId;
    private final Object2IntMap<T> toId;

    IdDispatchCodec(Function<V, ? extends T> function, List<IdDispatchCodec.b<B, V, T>> list, Object2IntMap<T> object2intmap) {
        this.typeGetter = function;
        this.byId = list;
        this.toId = object2intmap;
    }

    public V decode(B b0) {
        int i = VarInt.read(b0);

        if (i >= 0 && i < this.byId.size()) {
            IdDispatchCodec.b<B, V, T> iddispatchcodec_b = (IdDispatchCodec.b) this.byId.get(i);

            try {
                return iddispatchcodec_b.serializer.decode(b0);
            } catch (Exception exception) {
                throw new DecoderException("Failed to decode packet '" + String.valueOf(iddispatchcodec_b.type) + "'", exception);
            }
        } else {
            throw new DecoderException("Received unknown packet id " + i);
        }
    }

    public void encode(B b0, V v0) {
        T t0 = this.typeGetter.apply(v0);
        int i = this.toId.getOrDefault(t0, -1);

        if (i == -1) {
            throw new EncoderException("Sending unknown packet '" + String.valueOf(t0) + "'");
        } else {
            VarInt.write(b0, i);
            IdDispatchCodec.b<B, V, T> iddispatchcodec_b = (IdDispatchCodec.b) this.byId.get(i);

            try {
                StreamCodec<? super B, V> streamcodec = iddispatchcodec_b.serializer;

                streamcodec.encode(b0, v0);
            } catch (Exception exception) {
                throw new EncoderException("Failed to encode packet '" + String.valueOf(t0) + "'", exception);
            }
        }
    }

    public static <B extends ByteBuf, V, T> IdDispatchCodec.a<B, V, T> builder(Function<V, ? extends T> function) {
        return new IdDispatchCodec.a<>(function);
    }

    private static record b<B, V, T>(StreamCodec<? super B, ? extends V> serializer, T type) {

    }

    public static class a<B extends ByteBuf, V, T> {

        private final List<IdDispatchCodec.b<B, V, T>> entries = new ArrayList();
        private final Function<V, ? extends T> typeGetter;

        a(Function<V, ? extends T> function) {
            this.typeGetter = function;
        }

        public IdDispatchCodec.a<B, V, T> add(T t0, StreamCodec<? super B, ? extends V> streamcodec) {
            this.entries.add(new IdDispatchCodec.b<>(streamcodec, t0));
            return this;
        }

        public IdDispatchCodec<B, V, T> build() {
            Object2IntOpenHashMap<T> object2intopenhashmap = new Object2IntOpenHashMap();

            object2intopenhashmap.defaultReturnValue(-2);
            Iterator iterator = this.entries.iterator();

            IdDispatchCodec.b iddispatchcodec_b;
            int i;

            do {
                if (!iterator.hasNext()) {
                    return new IdDispatchCodec<>(this.typeGetter, List.copyOf(this.entries), object2intopenhashmap);
                }

                iddispatchcodec_b = (IdDispatchCodec.b) iterator.next();
                int j = object2intopenhashmap.size();

                i = object2intopenhashmap.putIfAbsent(iddispatchcodec_b.type, j);
            } while (i == -2);

            throw new IllegalStateException("Duplicate registration for type " + String.valueOf(iddispatchcodec_b.type));
        }
    }
}
