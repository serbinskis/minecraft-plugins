package net.minecraft.network.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface StreamCodec<B, V> extends StreamDecoder<B, V>, StreamEncoder<B, V> {

    static <B, V> StreamCodec<B, V> of(final StreamEncoder<B, V> streamencoder, final StreamDecoder<B, V> streamdecoder) {
        return new StreamCodec<B, V>() {
            @Override
            public V decode(B b0) {
                return streamdecoder.decode(b0);
            }

            @Override
            public void encode(B b0, V v0) {
                streamencoder.encode(b0, v0);
            }
        };
    }

    static <B, V> StreamCodec<B, V> ofMember(final StreamMemberEncoder<B, V> streammemberencoder, final StreamDecoder<B, V> streamdecoder) {
        return new StreamCodec<B, V>() {
            @Override
            public V decode(B b0) {
                return streamdecoder.decode(b0);
            }

            @Override
            public void encode(B b0, V v0) {
                streammemberencoder.encode(v0, b0);
            }
        };
    }

    static <B, V> StreamCodec<B, V> unit(final V v0) {
        return new StreamCodec<B, V>() {
            @Override
            public V decode(B b0) {
                return v0;
            }

            @Override
            public void encode(B b0, V v1) {
                if (!v1.equals(v0)) {
                    String s = String.valueOf(v1);

                    throw new IllegalStateException("Can't encode '" + s + "', expected '" + String.valueOf(v0) + "'");
                }
            }
        };
    }

    default <O> StreamCodec<B, O> apply(StreamCodec.a<B, V, O> streamcodec_a) {
        return streamcodec_a.apply(this);
    }

    default <O> StreamCodec<B, O> map(final Function<? super V, ? extends O> function, final Function<? super O, ? extends V> function1) {
        return new StreamCodec<B, O>() {
            @Override
            public O decode(B b0) {
                return function.apply(StreamCodec.this.decode(b0));
            }

            @Override
            public void encode(B b0, O o0) {
                StreamCodec.this.encode(b0, function1.apply(o0));
            }
        };
    }

    default <O extends ByteBuf> StreamCodec<O, V> mapStream(final Function<O, ? extends B> function) {
        return new StreamCodec<O, V>() {
            public V decode(O o0) {
                B b0 = function.apply(o0);

                return StreamCodec.this.decode(b0);
            }

            public void encode(O o0, V v0) {
                B b0 = function.apply(o0);

                StreamCodec.this.encode(b0, v0);
            }
        };
    }

    default <U> StreamCodec<B, U> dispatch(final Function<? super U, ? extends V> function, final Function<? super V, ? extends StreamCodec<? super B, ? extends U>> function1) {
        return new StreamCodec<B, U>() {
            @Override
            public U decode(B b0) {
                V v0 = StreamCodec.this.decode(b0);
                StreamCodec<? super B, ? extends U> streamcodec = (StreamCodec) function1.apply(v0);

                return streamcodec.decode(b0);
            }

            @Override
            public void encode(B b0, U u0) {
                V v0 = function.apply(u0);
                StreamCodec<B, U> streamcodec = (StreamCodec) function1.apply(v0);

                StreamCodec.this.encode(b0, v0);
                streamcodec.encode(b0, u0);
            }
        };
    }

    static <B, C, T1> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamcodec, final Function<C, T1> function, final Function<T1, C> function1) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B b0) {
                T1 t1 = streamcodec.decode(b0);

                return function1.apply(t1);
            }

            @Override
            public void encode(B b0, C c0) {
                streamcodec.encode(b0, function.apply(c0));
            }
        };
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamcodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamcodec1, final Function<C, T2> function1, final BiFunction<T1, T2, C> bifunction) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B b0) {
                T1 t1 = streamcodec.decode(b0);
                T2 t2 = streamcodec1.decode(b0);

                return bifunction.apply(t1, t2);
            }

            @Override
            public void encode(B b0, C c0) {
                streamcodec.encode(b0, function.apply(c0));
                streamcodec1.encode(b0, function1.apply(c0));
            }
        };
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamcodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamcodec1, final Function<C, T2> function1, final StreamCodec<? super B, T3> streamcodec2, final Function<C, T3> function2, final Function3<T1, T2, T3, C> function3) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B b0) {
                T1 t1 = streamcodec.decode(b0);
                T2 t2 = streamcodec1.decode(b0);
                T3 t3 = streamcodec2.decode(b0);

                return function3.apply(t1, t2, t3);
            }

            @Override
            public void encode(B b0, C c0) {
                streamcodec.encode(b0, function.apply(c0));
                streamcodec1.encode(b0, function1.apply(c0));
                streamcodec2.encode(b0, function2.apply(c0));
            }
        };
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamcodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamcodec1, final Function<C, T2> function1, final StreamCodec<? super B, T3> streamcodec2, final Function<C, T3> function2, final StreamCodec<? super B, T4> streamcodec3, final Function<C, T4> function3, final Function4<T1, T2, T3, T4, C> function4) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B b0) {
                T1 t1 = streamcodec.decode(b0);
                T2 t2 = streamcodec1.decode(b0);
                T3 t3 = streamcodec2.decode(b0);
                T4 t4 = streamcodec3.decode(b0);

                return function4.apply(t1, t2, t3, t4);
            }

            @Override
            public void encode(B b0, C c0) {
                streamcodec.encode(b0, function.apply(c0));
                streamcodec1.encode(b0, function1.apply(c0));
                streamcodec2.encode(b0, function2.apply(c0));
                streamcodec3.encode(b0, function3.apply(c0));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamcodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamcodec1, final Function<C, T2> function1, final StreamCodec<? super B, T3> streamcodec2, final Function<C, T3> function2, final StreamCodec<? super B, T4> streamcodec3, final Function<C, T4> function3, final StreamCodec<? super B, T5> streamcodec4, final Function<C, T5> function4, final Function5<T1, T2, T3, T4, T5, C> function5) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B b0) {
                T1 t1 = streamcodec.decode(b0);
                T2 t2 = streamcodec1.decode(b0);
                T3 t3 = streamcodec2.decode(b0);
                T4 t4 = streamcodec3.decode(b0);
                T5 t5 = streamcodec4.decode(b0);

                return function5.apply(t1, t2, t3, t4, t5);
            }

            @Override
            public void encode(B b0, C c0) {
                streamcodec.encode(b0, function.apply(c0));
                streamcodec1.encode(b0, function1.apply(c0));
                streamcodec2.encode(b0, function2.apply(c0));
                streamcodec3.encode(b0, function3.apply(c0));
                streamcodec4.encode(b0, function4.apply(c0));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamcodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamcodec1, final Function<C, T2> function1, final StreamCodec<? super B, T3> streamcodec2, final Function<C, T3> function2, final StreamCodec<? super B, T4> streamcodec3, final Function<C, T4> function3, final StreamCodec<? super B, T5> streamcodec4, final Function<C, T5> function4, final StreamCodec<? super B, T6> streamcodec5, final Function<C, T6> function5, final Function6<T1, T2, T3, T4, T5, T6, C> function6) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B b0) {
                T1 t1 = streamcodec.decode(b0);
                T2 t2 = streamcodec1.decode(b0);
                T3 t3 = streamcodec2.decode(b0);
                T4 t4 = streamcodec3.decode(b0);
                T5 t5 = streamcodec4.decode(b0);
                T6 t6 = streamcodec5.decode(b0);

                return function6.apply(t1, t2, t3, t4, t5, t6);
            }

            @Override
            public void encode(B b0, C c0) {
                streamcodec.encode(b0, function.apply(c0));
                streamcodec1.encode(b0, function1.apply(c0));
                streamcodec2.encode(b0, function2.apply(c0));
                streamcodec3.encode(b0, function3.apply(c0));
                streamcodec4.encode(b0, function4.apply(c0));
                streamcodec5.encode(b0, function5.apply(c0));
            }
        };
    }

    static <B, T> StreamCodec<B, T> recursive(final UnaryOperator<StreamCodec<B, T>> unaryoperator) {
        return new StreamCodec<B, T>() {
            private final Supplier<StreamCodec<B, T>> inner = Suppliers.memoize(() -> {
                return (StreamCodec) unaryoperator.apply(this);
            });

            @Override
            public T decode(B b0) {
                return ((StreamCodec) this.inner.get()).decode(b0);
            }

            @Override
            public void encode(B b0, T t0) {
                ((StreamCodec) this.inner.get()).encode(b0, t0);
            }
        };
    }

    default <S extends B> StreamCodec<S, V> cast() {
        return this;
    }

    @FunctionalInterface
    public interface a<B, S, T> {

        StreamCodec<B, T> apply(StreamCodec<B, S> streamcodec);
    }
}
