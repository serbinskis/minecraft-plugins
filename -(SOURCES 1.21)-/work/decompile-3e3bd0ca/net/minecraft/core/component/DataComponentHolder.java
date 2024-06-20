package net.minecraft.core.component;

import javax.annotation.Nullable;

public interface DataComponentHolder {

    DataComponentMap getComponents();

    @Nullable
    default <T> T get(DataComponentType<? extends T> datacomponenttype) {
        return this.getComponents().get(datacomponenttype);
    }

    default <T> T getOrDefault(DataComponentType<? extends T> datacomponenttype, T t0) {
        return this.getComponents().getOrDefault(datacomponenttype, t0);
    }

    default boolean has(DataComponentType<?> datacomponenttype) {
        return this.getComponents().has(datacomponenttype);
    }
}
