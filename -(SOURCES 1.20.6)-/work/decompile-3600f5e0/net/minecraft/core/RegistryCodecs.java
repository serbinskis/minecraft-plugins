package net.minecraft.core;

import com.mojang.serialization.Codec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {

    public RegistryCodecs() {}

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends IRegistry<E>> resourcekey, Codec<E> codec) {
        return homogeneousList(resourcekey, codec, false);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends IRegistry<E>> resourcekey, Codec<E> codec, boolean flag) {
        return HolderSetCodec.create(resourcekey, RegistryFileCodec.create(resourcekey, codec), flag);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends IRegistry<E>> resourcekey) {
        return homogeneousList(resourcekey, false);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends IRegistry<E>> resourcekey, boolean flag) {
        return HolderSetCodec.create(resourcekey, RegistryFixedCodec.create(resourcekey), flag);
    }
}
