package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceKey;

public interface IRegistryWritable<T> extends IRegistry<T> {

    Holder.c<T> register(ResourceKey<T> resourcekey, T t0, Lifecycle lifecycle);

    boolean isEmpty();

    HolderGetter<T> createRegistrationLookup();
}
