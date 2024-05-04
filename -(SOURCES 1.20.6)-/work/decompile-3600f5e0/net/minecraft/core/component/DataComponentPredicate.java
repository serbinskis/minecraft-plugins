package net.minecraft.core.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class DataComponentPredicate implements Predicate<DataComponentMap> {

    public static final Codec<DataComponentPredicate> CODEC = DataComponentType.VALUE_MAP_CODEC.xmap((map) -> {
        return new DataComponentPredicate((List) map.entrySet().stream().map(TypedDataComponent::fromEntryUnchecked).collect(Collectors.toList()));
    }, (datacomponentpredicate) -> {
        return (Map) datacomponentpredicate.expectedComponents.stream().filter((typeddatacomponent) -> {
            return !typeddatacomponent.type().isTransient();
        }).collect(Collectors.toMap(TypedDataComponent::type, TypedDataComponent::value));
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate> STREAM_CODEC = TypedDataComponent.STREAM_CODEC.apply(ByteBufCodecs.list()).map(DataComponentPredicate::new, (datacomponentpredicate) -> {
        return datacomponentpredicate.expectedComponents;
    });
    public static final DataComponentPredicate EMPTY = new DataComponentPredicate(List.of());
    private final List<TypedDataComponent<?>> expectedComponents;

    DataComponentPredicate(List<TypedDataComponent<?>> list) {
        this.expectedComponents = list;
    }

    public static DataComponentPredicate.a builder() {
        return new DataComponentPredicate.a();
    }

    public static DataComponentPredicate allOf(DataComponentMap datacomponentmap) {
        return new DataComponentPredicate(ImmutableList.copyOf(datacomponentmap));
    }

    public boolean equals(Object object) {
        boolean flag;

        if (object instanceof DataComponentPredicate datacomponentpredicate) {
            if (this.expectedComponents.equals(datacomponentpredicate.expectedComponents)) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    public int hashCode() {
        return this.expectedComponents.hashCode();
    }

    public String toString() {
        return this.expectedComponents.toString();
    }

    public boolean test(DataComponentMap datacomponentmap) {
        Iterator iterator = this.expectedComponents.iterator();

        TypedDataComponent typeddatacomponent;
        Object object;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            typeddatacomponent = (TypedDataComponent) iterator.next();
            object = datacomponentmap.get(typeddatacomponent.type());
        } while (Objects.equals(typeddatacomponent.value(), object));

        return false;
    }

    public boolean test(DataComponentHolder datacomponentholder) {
        return this.test(datacomponentholder.getComponents());
    }

    public boolean alwaysMatches() {
        return this.expectedComponents.isEmpty();
    }

    public DataComponentPatch asPatch() {
        DataComponentPatch.a datacomponentpatch_a = DataComponentPatch.builder();
        Iterator iterator = this.expectedComponents.iterator();

        while (iterator.hasNext()) {
            TypedDataComponent<?> typeddatacomponent = (TypedDataComponent) iterator.next();

            datacomponentpatch_a.set(typeddatacomponent);
        }

        return datacomponentpatch_a.build();
    }

    public static class a {

        private final List<TypedDataComponent<?>> expectedComponents = new ArrayList();

        a() {}

        public <T> DataComponentPredicate.a expect(DataComponentType<? super T> datacomponenttype, T t0) {
            Iterator iterator = this.expectedComponents.iterator();

            TypedDataComponent typeddatacomponent;

            do {
                if (!iterator.hasNext()) {
                    this.expectedComponents.add(new TypedDataComponent<>(datacomponenttype, t0));
                    return this;
                }

                typeddatacomponent = (TypedDataComponent) iterator.next();
            } while (typeddatacomponent.type() != datacomponenttype);

            throw new IllegalArgumentException("Predicate already has component of type: '" + String.valueOf(datacomponenttype) + "'");
        }

        public DataComponentPredicate build() {
            return new DataComponentPredicate(List.copyOf(this.expectedComponents));
        }
    }
}
