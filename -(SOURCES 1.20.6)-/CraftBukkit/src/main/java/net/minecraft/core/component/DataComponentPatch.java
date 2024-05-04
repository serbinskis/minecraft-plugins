package net.minecraft.core.component;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.Unit;

public final class DataComponentPatch {

    public static final DataComponentPatch EMPTY = new DataComponentPatch(Reference2ObjectMaps.emptyMap());
    public static final Codec<DataComponentPatch> CODEC = Codec.dispatchedMap(DataComponentPatch.b.CODEC, DataComponentPatch.b::valueCodec).xmap((map) -> {
        if (map.isEmpty()) {
            return DataComponentPatch.EMPTY;
        } else {
            Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap = new Reference2ObjectArrayMap(map.size());
            Iterator iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<DataComponentPatch.b, ?> entry = (Entry) iterator.next();
                DataComponentPatch.b datacomponentpatch_b = (DataComponentPatch.b) entry.getKey();

                if (datacomponentpatch_b.removed()) {
                    reference2objectmap.put(datacomponentpatch_b.type(), Optional.empty());
                } else {
                    reference2objectmap.put(datacomponentpatch_b.type(), Optional.of(entry.getValue()));
                }
            }

            return new DataComponentPatch(reference2objectmap);
        }
    }, (datacomponentpatch) -> {
        Reference2ObjectMap<DataComponentPatch.b, Object> reference2objectmap = new Reference2ObjectArrayMap(datacomponentpatch.map.size());
        ObjectIterator objectiterator = Reference2ObjectMaps.fastIterable(datacomponentpatch.map).iterator();

        while (objectiterator.hasNext()) {
            Entry<DataComponentType<?>, Optional<?>> entry = (Entry) objectiterator.next();
            DataComponentType<?> datacomponenttype = (DataComponentType) entry.getKey();

            if (!datacomponenttype.isTransient()) {
                Optional<?> optional = (Optional) entry.getValue();

                if (optional.isPresent()) {
                    reference2objectmap.put(new DataComponentPatch.b(datacomponenttype, false), optional.get());
                } else {
                    reference2objectmap.put(new DataComponentPatch.b(datacomponenttype, true), Unit.INSTANCE);
                }
            }
        }

        return (Reference2ObjectMap) reference2objectmap; // CraftBukkit - decompile error
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch>() {
        public DataComponentPatch decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            int i = registryfriendlybytebuf.readVarInt();
            int j = registryfriendlybytebuf.readVarInt();

            if (i == 0 && j == 0) {
                return DataComponentPatch.EMPTY;
            } else {
                Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap = new Reference2ObjectArrayMap(i + j);

                DataComponentType datacomponenttype;
                int k;

                for (k = 0; k < i; ++k) {
                    datacomponenttype = (DataComponentType) DataComponentType.STREAM_CODEC.decode(registryfriendlybytebuf);
                    Object object = datacomponenttype.streamCodec().decode(registryfriendlybytebuf);

                    reference2objectmap.put(datacomponenttype, Optional.of(object));
                }

                for (k = 0; k < j; ++k) {
                    datacomponenttype = (DataComponentType) DataComponentType.STREAM_CODEC.decode(registryfriendlybytebuf);
                    reference2objectmap.put(datacomponenttype, Optional.empty());
                }

                return new DataComponentPatch(reference2objectmap);
            }
        }

        public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, DataComponentPatch datacomponentpatch) {
            if (datacomponentpatch.isEmpty()) {
                registryfriendlybytebuf.writeVarInt(0);
                registryfriendlybytebuf.writeVarInt(0);
            } else {
                int i = 0;
                int j = 0;
                ObjectIterator objectiterator = Reference2ObjectMaps.fastIterable(datacomponentpatch.map).iterator();

                it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry it_unimi_dsi_fastutil_objects_reference2objectmap_entry;

                while (objectiterator.hasNext()) {
                    it_unimi_dsi_fastutil_objects_reference2objectmap_entry = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry) objectiterator.next();
                    if (((Optional) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getValue()).isPresent()) {
                        ++i;
                    } else {
                        ++j;
                    }
                }

                registryfriendlybytebuf.writeVarInt(i);
                registryfriendlybytebuf.writeVarInt(j);
                objectiterator = Reference2ObjectMaps.fastIterable(datacomponentpatch.map).iterator();

                while (objectiterator.hasNext()) {
                    it_unimi_dsi_fastutil_objects_reference2objectmap_entry = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry) objectiterator.next();
                    Optional<?> optional = (Optional) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getValue();

                    if (optional.isPresent()) {
                        DataComponentType<?> datacomponenttype = (DataComponentType) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getKey();

                        DataComponentType.STREAM_CODEC.encode(registryfriendlybytebuf, datacomponenttype);
                        encodeComponent(registryfriendlybytebuf, datacomponenttype, optional.get());
                    }
                }

                objectiterator = Reference2ObjectMaps.fastIterable(datacomponentpatch.map).iterator();

                while (objectiterator.hasNext()) {
                    it_unimi_dsi_fastutil_objects_reference2objectmap_entry = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry) objectiterator.next();
                    if (((Optional) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getValue()).isEmpty()) {
                        DataComponentType<?> datacomponenttype1 = (DataComponentType) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getKey();

                        DataComponentType.STREAM_CODEC.encode(registryfriendlybytebuf, datacomponenttype1);
                    }
                }

            }
        }

        private static <T> void encodeComponent(RegistryFriendlyByteBuf registryfriendlybytebuf, DataComponentType<T> datacomponenttype, Object object) {
            datacomponenttype.streamCodec().encode(registryfriendlybytebuf, (T) object); // CraftBukkit - decompile error
        }
    };
    private static final String REMOVED_PREFIX = "!";
    final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    DataComponentPatch(Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap) {
        this.map = reference2objectmap;
    }

    public static DataComponentPatch.a builder() {
        return new DataComponentPatch.a();
    }

    @Nullable
    public <T> Optional<? extends T> get(DataComponentType<? extends T> datacomponenttype) {
        return (Optional) this.map.get(datacomponenttype);
    }

    public Set<Entry<DataComponentType<?>, Optional<?>>> entrySet() {
        return this.map.entrySet();
    }

    public int size() {
        return this.map.size();
    }

    public DataComponentPatch forget(Predicate<DataComponentType<?>> predicate) {
        if (this.isEmpty()) {
            return DataComponentPatch.EMPTY;
        } else {
            Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap = new Reference2ObjectArrayMap(this.map);

            reference2objectmap.keySet().removeIf(predicate);
            return reference2objectmap.isEmpty() ? DataComponentPatch.EMPTY : new DataComponentPatch(reference2objectmap);
        }
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public DataComponentPatch.c split() {
        if (this.isEmpty()) {
            return DataComponentPatch.c.EMPTY;
        } else {
            DataComponentMap.a datacomponentmap_a = DataComponentMap.builder();
            Set<DataComponentType<?>> set = Sets.newIdentityHashSet();

            this.map.forEach((datacomponenttype, optional) -> {
                if (optional.isPresent()) {
                    datacomponentmap_a.setUnchecked(datacomponenttype, optional.get());
                } else {
                    set.add(datacomponenttype);
                }

            });
            return new DataComponentPatch.c(datacomponentmap_a.build(), set);
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof DataComponentPatch) {
                DataComponentPatch datacomponentpatch = (DataComponentPatch) object;

                if (this.map.equals(datacomponentpatch.map)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    public String toString() {
        return toString(this.map);
    }

    static String toString(Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap) {
        StringBuilder stringbuilder = new StringBuilder();

        stringbuilder.append('{');
        boolean flag = true;
        ObjectIterator objectiterator = Reference2ObjectMaps.fastIterable(reference2objectmap).iterator();

        while (objectiterator.hasNext()) {
            Entry<DataComponentType<?>, Optional<?>> entry = (Entry) objectiterator.next();

            if (flag) {
                flag = false;
            } else {
                stringbuilder.append(", ");
            }

            Optional<?> optional = (Optional) entry.getValue();

            if (optional.isPresent()) {
                stringbuilder.append(entry.getKey());
                stringbuilder.append("=>");
                stringbuilder.append(optional.get());
            } else {
                stringbuilder.append("!");
                stringbuilder.append(entry.getKey());
            }
        }

        stringbuilder.append('}');
        return stringbuilder.toString();
    }

    public static class a {

        private final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map = new Reference2ObjectArrayMap();

        a() {}

        // CraftBukkit start
        public void copy(DataComponentPatch orig) {
            this.map.putAll(orig.map);
        }

        public void clear(DataComponentType<?> type) {
            this.map.remove(type);
        }

        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }

            if (object instanceof DataComponentPatch.a patch) {
                return this.map.equals(patch.map);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return this.map.hashCode();
        }
        // CraftBukkit end

        public <T> DataComponentPatch.a set(DataComponentType<T> datacomponenttype, T t0) {
            this.map.put(datacomponenttype, Optional.of(t0));
            return this;
        }

        public <T> DataComponentPatch.a remove(DataComponentType<T> datacomponenttype) {
            this.map.put(datacomponenttype, Optional.empty());
            return this;
        }

        public <T> DataComponentPatch.a set(TypedDataComponent<T> typeddatacomponent) {
            return this.set(typeddatacomponent.type(), typeddatacomponent.value());
        }

        public DataComponentPatch build() {
            return this.map.isEmpty() ? DataComponentPatch.EMPTY : new DataComponentPatch(this.map);
        }
    }

    public static record c(DataComponentMap added, Set<DataComponentType<?>> removed) {

        public static final DataComponentPatch.c EMPTY = new DataComponentPatch.c(DataComponentMap.EMPTY, Set.of());
    }

    private static record b(DataComponentType<?> type, boolean removed) {

        public static final Codec<DataComponentPatch.b> CODEC = Codec.STRING.flatXmap((s) -> {
            boolean flag = s.startsWith("!");

            if (flag) {
                s = s.substring("!".length());
            }

            MinecraftKey minecraftkey = MinecraftKey.tryParse(s);
            DataComponentType<?> datacomponenttype = (DataComponentType) BuiltInRegistries.DATA_COMPONENT_TYPE.get(minecraftkey);

            return datacomponenttype == null ? DataResult.error(() -> {
                return "No component with type: '" + String.valueOf(minecraftkey) + "'";
            }) : (datacomponenttype.isTransient() ? DataResult.error(() -> {
                return "'" + String.valueOf(minecraftkey) + "' is not a persistent component";
            }) : DataResult.success(new DataComponentPatch.b(datacomponenttype, flag)));
        }, (datacomponentpatch_b) -> {
            DataComponentType<?> datacomponenttype = datacomponentpatch_b.type();
            MinecraftKey minecraftkey = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(datacomponenttype);

            return minecraftkey == null ? DataResult.error(() -> {
                return "Unregistered component: " + String.valueOf(datacomponenttype);
            }) : DataResult.success(datacomponentpatch_b.removed() ? "!" + String.valueOf(minecraftkey) : minecraftkey.toString());
        });

        public Codec<?> valueCodec() {
            return this.removed ? Codec.EMPTY.codec() : this.type.codecOrThrow();
        }
    }
}
