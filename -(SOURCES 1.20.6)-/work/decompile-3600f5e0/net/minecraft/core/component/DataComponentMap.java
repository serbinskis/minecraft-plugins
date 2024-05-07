package net.minecraft.core.component;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

public interface DataComponentMap extends Iterable<TypedDataComponent<?>> {

    DataComponentMap EMPTY = new DataComponentMap() {
        @Nullable
        @Override
        public <T> T get(DataComponentType<? extends T> datacomponenttype) {
            return null;
        }

        @Override
        public Set<DataComponentType<?>> keySet() {
            return Set.of();
        }

        @Override
        public Iterator<TypedDataComponent<?>> iterator() {
            return Collections.emptyIterator();
        }
    };
    Codec<DataComponentMap> CODEC = DataComponentType.VALUE_MAP_CODEC.flatComapMap(DataComponentMap.a::buildFromMapTrusted, (datacomponentmap) -> {
        int i = datacomponentmap.size();

        if (i == 0) {
            return DataResult.success(Reference2ObjectMaps.emptyMap());
        } else {
            Reference2ObjectMap<DataComponentType<?>, Object> reference2objectmap = new Reference2ObjectArrayMap(i);
            Iterator iterator = datacomponentmap.iterator();

            while (iterator.hasNext()) {
                TypedDataComponent<?> typeddatacomponent = (TypedDataComponent) iterator.next();

                if (!typeddatacomponent.type().isTransient()) {
                    reference2objectmap.put(typeddatacomponent.type(), typeddatacomponent.value());
                }
            }

            return DataResult.success(reference2objectmap);
        }
    });

    static DataComponentMap composite(final DataComponentMap datacomponentmap, final DataComponentMap datacomponentmap1) {
        return new DataComponentMap() {
            @Nullable
            @Override
            public <T> T get(DataComponentType<? extends T> datacomponenttype) {
                T t0 = datacomponentmap1.get(datacomponenttype);

                return t0 != null ? t0 : datacomponentmap.get(datacomponenttype);
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                return Sets.union(datacomponentmap.keySet(), datacomponentmap1.keySet());
            }
        };
    }

    static DataComponentMap.a builder() {
        return new DataComponentMap.a();
    }

    @Nullable
    <T> T get(DataComponentType<? extends T> datacomponenttype);

    Set<DataComponentType<?>> keySet();

    default boolean has(DataComponentType<?> datacomponenttype) {
        return this.get(datacomponenttype) != null;
    }

    default <T> T getOrDefault(DataComponentType<? extends T> datacomponenttype, T t0) {
        T t1 = this.get(datacomponenttype);

        return t1 != null ? t1 : t0;
    }

    @Nullable
    default <T> TypedDataComponent<T> getTyped(DataComponentType<T> datacomponenttype) {
        T t0 = this.get(datacomponenttype);

        return t0 != null ? new TypedDataComponent<>(datacomponenttype, t0) : null;
    }

    default Iterator<TypedDataComponent<?>> iterator() {
        return Iterators.transform(this.keySet().iterator(), (datacomponenttype) -> {
            return (TypedDataComponent) Objects.requireNonNull(this.getTyped(datacomponenttype));
        });
    }

    default Stream<TypedDataComponent<?>> stream() {
        return StreamSupport.stream(Spliterators.spliterator(this.iterator(), (long) this.size(), 1345), false);
    }

    default int size() {
        return this.keySet().size();
    }

    default boolean isEmpty() {
        return this.size() == 0;
    }

    default DataComponentMap filter(final Predicate<DataComponentType<?>> predicate) {
        return new DataComponentMap() {
            @Nullable
            @Override
            public <T> T get(DataComponentType<? extends T> datacomponenttype) {
                return predicate.test(datacomponenttype) ? DataComponentMap.this.get(datacomponenttype) : null;
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                Set set = DataComponentMap.this.keySet();
                Predicate predicate1 = predicate;

                Objects.requireNonNull(predicate);
                return Sets.filter(set, predicate1::test);
            }
        };
    }

    public static class a {

        private final Reference2ObjectMap<DataComponentType<?>, Object> map = new Reference2ObjectArrayMap();

        a() {}

        public <T> DataComponentMap.a set(DataComponentType<T> datacomponenttype, @Nullable T t0) {
            this.setUnchecked(datacomponenttype, t0);
            return this;
        }

        <T> void setUnchecked(DataComponentType<T> datacomponenttype, @Nullable Object object) {
            if (object != null) {
                this.map.put(datacomponenttype, object);
            } else {
                this.map.remove(datacomponenttype);
            }

        }

        public DataComponentMap.a addAll(DataComponentMap datacomponentmap) {
            Iterator iterator = datacomponentmap.iterator();

            while (iterator.hasNext()) {
                TypedDataComponent<?> typeddatacomponent = (TypedDataComponent) iterator.next();

                this.map.put(typeddatacomponent.type(), typeddatacomponent.value());
            }

            return this;
        }

        public DataComponentMap build() {
            return buildFromMapTrusted(this.map);
        }

        private static DataComponentMap buildFromMapTrusted(Map<DataComponentType<?>, Object> map) {
            return (DataComponentMap) (map.isEmpty() ? DataComponentMap.EMPTY : (map.size() < 8 ? new DataComponentMap.a.a(new Reference2ObjectArrayMap(map)) : new DataComponentMap.a.a(new Reference2ObjectOpenHashMap(map))));
        }

        private static record a(Reference2ObjectMap<DataComponentType<?>, Object> map) implements DataComponentMap {

            @Nullable
            @Override
            public <T> T get(DataComponentType<? extends T> datacomponenttype) {
                return this.map.get(datacomponenttype);
            }

            @Override
            public boolean has(DataComponentType<?> datacomponenttype) {
                return this.map.containsKey(datacomponenttype);
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                return this.map.keySet();
            }

            @Override
            public Iterator<TypedDataComponent<?>> iterator() {
                return Iterators.transform(Reference2ObjectMaps.fastIterator(this.map), TypedDataComponent::fromEntryUnchecked);
            }

            @Override
            public int size() {
                return this.map.size();
            }

            public String toString() {
                return this.map.toString();
            }
        }
    }
}
