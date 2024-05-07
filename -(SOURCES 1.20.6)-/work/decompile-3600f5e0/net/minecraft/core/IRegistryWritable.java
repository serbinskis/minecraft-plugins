package net.minecraft.core;

import net.minecraft.resources.ResourceKey;

public interface IRegistryWritable<T> extends IRegistry<T> {

    Holder.c<T> register(ResourceKey<T> resourcekey, T t0, RegistrationInfo registrationinfo);

    boolean isEmpty();

    HolderGetter<T> createRegistrationLookup();
}
