package net.minecraft.world.level.block.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.IBlockState;

public abstract class IBlockDataHolder<O, S> {

    public static final String NAME_TAG = "Name";
    public static final String PROPERTIES_TAG = "Properties";
    public static final Function<Entry<IBlockState<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Entry<IBlockState<?>, Comparable<?>>, String>() {
        public String apply(@Nullable Entry<IBlockState<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            } else {
                IBlockState<?> iblockstate = (IBlockState) entry.getKey();
                String s = iblockstate.getName();

                return s + "=" + this.getName(iblockstate, (Comparable) entry.getValue());
            }
        }

        private <T extends Comparable<T>> String getName(IBlockState<T> iblockstate, Comparable<?> comparable) {
            return iblockstate.getName(comparable);
        }
    };
    protected final O owner;
    private final Reference2ObjectArrayMap<IBlockState<?>, Comparable<?>> values;
    private Table<IBlockState<?>, Comparable<?>, S> neighbours;
    protected final MapCodec<S> propertiesCodec;

    protected IBlockDataHolder(O o0, Reference2ObjectArrayMap<IBlockState<?>, Comparable<?>> reference2objectarraymap, MapCodec<S> mapcodec) {
        this.owner = o0;
        this.values = reference2objectarraymap;
        this.propertiesCodec = mapcodec;
    }

    public <T extends Comparable<T>> S cycle(IBlockState<T> iblockstate) {
        return this.setValue(iblockstate, (Comparable) findNextInCollection(iblockstate.getPossibleValues(), this.getValue(iblockstate)));
    }

    protected static <T> T findNextInCollection(Collection<T> collection, T t0) {
        Iterator<T> iterator = collection.iterator();

        do {
            if (!iterator.hasNext()) {
                return iterator.next();
            }
        } while (!iterator.next().equals(t0));

        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return collection.iterator().next();
        }
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();

        stringbuilder.append(this.owner);
        if (!this.getValues().isEmpty()) {
            stringbuilder.append('[');
            stringbuilder.append((String) this.getValues().entrySet().stream().map(IBlockDataHolder.PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            stringbuilder.append(']');
        }

        return stringbuilder.toString();
    }

    public Collection<IBlockState<?>> getProperties() {
        return Collections.unmodifiableCollection(this.values.keySet());
    }

    public <T extends Comparable<T>> boolean hasProperty(IBlockState<T> iblockstate) {
        return this.values.containsKey(iblockstate);
    }

    public <T extends Comparable<T>> T getValue(IBlockState<T> iblockstate) {
        Comparable<?> comparable = (Comparable) this.values.get(iblockstate);

        if (comparable == null) {
            String s = String.valueOf(iblockstate);

            throw new IllegalArgumentException("Cannot get property " + s + " as it does not exist in " + String.valueOf(this.owner));
        } else {
            return (Comparable) iblockstate.getValueClass().cast(comparable);
        }
    }

    public <T extends Comparable<T>> Optional<T> getOptionalValue(IBlockState<T> iblockstate) {
        Comparable<?> comparable = (Comparable) this.values.get(iblockstate);

        return comparable == null ? Optional.empty() : Optional.of((Comparable) iblockstate.getValueClass().cast(comparable));
    }

    public <T extends Comparable<T>, V extends T> S setValue(IBlockState<T> iblockstate, V v0) {
        Comparable<?> comparable = (Comparable) this.values.get(iblockstate);
        String s;

        if (comparable == null) {
            s = String.valueOf(iblockstate);
            throw new IllegalArgumentException("Cannot set property " + s + " as it does not exist in " + String.valueOf(this.owner));
        } else if (comparable.equals(v0)) {
            return this;
        } else {
            S s0 = this.neighbours.get(iblockstate, v0);

            if (s0 == null) {
                s = String.valueOf(iblockstate);
                throw new IllegalArgumentException("Cannot set property " + s + " to " + String.valueOf(v0) + " on " + String.valueOf(this.owner) + ", it is not an allowed value");
            } else {
                return s0;
            }
        }
    }

    public <T extends Comparable<T>, V extends T> S trySetValue(IBlockState<T> iblockstate, V v0) {
        Comparable<?> comparable = (Comparable) this.values.get(iblockstate);

        if (comparable != null && !comparable.equals(v0)) {
            S s0 = this.neighbours.get(iblockstate, v0);

            if (s0 == null) {
                String s = String.valueOf(iblockstate);

                throw new IllegalArgumentException("Cannot set property " + s + " to " + String.valueOf(v0) + " on " + String.valueOf(this.owner) + ", it is not an allowed value");
            } else {
                return s0;
            }
        } else {
            return this;
        }
    }

    public void populateNeighbours(Map<Map<IBlockState<?>, Comparable<?>>, S> map) {
        if (this.neighbours != null) {
            throw new IllegalStateException();
        } else {
            Table<IBlockState<?>, Comparable<?>, S> table = HashBasedTable.create();
            ObjectIterator objectiterator = this.values.entrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<IBlockState<?>, Comparable<?>> entry = (Entry) objectiterator.next();
                IBlockState<?> iblockstate = (IBlockState) entry.getKey();
                Iterator iterator = iblockstate.getPossibleValues().iterator();

                while (iterator.hasNext()) {
                    Comparable<?> comparable = (Comparable) iterator.next();

                    if (!comparable.equals(entry.getValue())) {
                        table.put(iblockstate, comparable, map.get(this.makeNeighbourValues(iblockstate, comparable)));
                    }
                }
            }

            this.neighbours = (Table) (table.isEmpty() ? table : ArrayTable.create(table));
        }
    }

    private Map<IBlockState<?>, Comparable<?>> makeNeighbourValues(IBlockState<?> iblockstate, Comparable<?> comparable) {
        Map<IBlockState<?>, Comparable<?>> map = new Reference2ObjectArrayMap(this.values);

        map.put(iblockstate, comparable);
        return map;
    }

    public Map<IBlockState<?>, Comparable<?>> getValues() {
        return this.values;
    }

    protected static <O, S extends IBlockDataHolder<O, S>> Codec<S> codec(Codec<O> codec, Function<O, S> function) {
        return codec.dispatch("Name", (iblockdataholder) -> {
            return iblockdataholder.owner;
        }, (object) -> {
            S s0 = (IBlockDataHolder) function.apply(object);

            return s0.getValues().isEmpty() ? MapCodec.unit(s0) : s0.propertiesCodec.codec().lenientOptionalFieldOf("Properties").xmap((optional) -> {
                return (IBlockDataHolder) optional.orElse(s0);
            }, Optional::of);
        });
    }
}
