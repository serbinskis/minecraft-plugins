package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;

public interface INamable {

    int PRE_BUILT_MAP_THRESHOLD = 16;

    String getSerializedName();

    static <E extends Enum<E> & INamable> INamable.a<E> fromEnum(Supplier<E[]> supplier) {
        return fromEnumWithMapping(supplier, (s) -> {
            return s;
        });
    }

    static <E extends Enum<E> & INamable> INamable.a<E> fromEnumWithMapping(Supplier<E[]> supplier, Function<String, String> function) {
        E[] ae = (Enum[]) supplier.get();
        Function<String, E> function1 = createNameLookup(ae, function);

        return new INamable.a<>(ae, function1);
    }

    static <T extends INamable> Codec<T> fromValues(Supplier<T[]> supplier) {
        T[] at = (INamable[]) supplier.get();
        Function<String, T> function = createNameLookup(at, (s) -> {
            return s;
        });
        ToIntFunction<T> tointfunction = SystemUtils.createIndexLookup(Arrays.asList(at));

        return new INamable.b<>(at, function, tointfunction);
    }

    static <T extends INamable> Function<String, T> createNameLookup(T[] at, Function<String, String> function) {
        if (at.length > 16) {
            Map<String, T> map = (Map) Arrays.stream(at).collect(Collectors.toMap((inamable) -> {
                return (String) function.apply(inamable.getSerializedName());
            }, (inamable) -> {
                return inamable;
            }));

            return (s) -> {
                return s == null ? null : (INamable) map.get(s);
            };
        } else {
            return (s) -> {
                INamable[] ainamable = at;
                int i = at.length;

                for (int j = 0; j < i; ++j) {
                    T t0 = ainamable[j];

                    if (((String) function.apply(t0.getSerializedName())).equals(s)) {
                        return t0;
                    }
                }

                return null;
            };
        }
    }

    static Keyable keys(final INamable[] ainamable) {
        return new Keyable() {
            public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
                Stream stream = Arrays.stream(ainamable).map(INamable::getSerializedName);

                Objects.requireNonNull(dynamicops);
                return stream.map(dynamicops::createString);
            }
        };
    }

    /** @deprecated */
    @Deprecated
    public static class a<E extends Enum<E> & INamable> extends INamable.b<E> {

        private final Function<String, E> resolver;

        public a(E[] ae, Function<String, E> function) {
            super(ae, function, (object) -> {
                return ((Enum) object).ordinal();
            });
            this.resolver = function;
        }

        @Nullable
        public E byName(@Nullable String s) {
            return (Enum) this.resolver.apply(s);
        }

        public E byName(@Nullable String s, E e0) {
            return (Enum) Objects.requireNonNullElse(this.byName(s), e0);
        }
    }

    public static class b<S extends INamable> implements Codec<S> {

        private final Codec<S> codec;

        public b(S[] as, Function<String, S> function, ToIntFunction<S> tointfunction) {
            this.codec = ExtraCodecs.orCompressed(Codec.stringResolver(INamable::getSerializedName, function), ExtraCodecs.idResolverCodec(tointfunction, (i) -> {
                return i >= 0 && i < as.length ? as[i] : null;
            }, -1));
        }

        public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> dynamicops, T t0) {
            return this.codec.decode(dynamicops, t0);
        }

        public <T> DataResult<T> encode(S s0, DynamicOps<T> dynamicops, T t0) {
            return this.codec.encode(s0, dynamicops, t0);
        }
    }
}
