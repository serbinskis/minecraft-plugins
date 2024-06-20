package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.repository.KnownPack;

public class RegistrySynchronization {

    public static final Set<ResourceKey<? extends IRegistry<?>>> NETWORKABLE_REGISTRIES = (Set) RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream().map(RegistryDataLoader.c::key).collect(Collectors.toUnmodifiableSet());

    public RegistrySynchronization() {}

    public static void packRegistries(DynamicOps<NBTBase> dynamicops, IRegistryCustom iregistrycustom, Set<KnownPack> set, BiConsumer<ResourceKey<? extends IRegistry<?>>, List<RegistrySynchronization.a>> biconsumer) {
        RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach((registrydataloader_c) -> {
            packRegistry(dynamicops, registrydataloader_c, iregistrycustom, set, biconsumer);
        });
    }

    private static <T> void packRegistry(DynamicOps<NBTBase> dynamicops, RegistryDataLoader.c<T> registrydataloader_c, IRegistryCustom iregistrycustom, Set<KnownPack> set, BiConsumer<ResourceKey<? extends IRegistry<?>>, List<RegistrySynchronization.a>> biconsumer) {
        iregistrycustom.registry(registrydataloader_c.key()).ifPresent((iregistry) -> {
            List<RegistrySynchronization.a> list = new ArrayList(iregistry.size());

            iregistry.holders().forEach((holder_c) -> {
                Optional optional = iregistry.registrationInfo(holder_c.key()).flatMap(RegistrationInfo::knownPackInfo);

                Objects.requireNonNull(set);
                boolean flag = optional.filter(set::contains).isPresent();
                Optional optional1;

                if (flag) {
                    optional1 = Optional.empty();
                } else {
                    NBTBase nbtbase = (NBTBase) registrydataloader_c.elementCodec().encodeStart(dynamicops, holder_c.value()).getOrThrow((s) -> {
                        String s1 = String.valueOf(holder_c.key());

                        return new IllegalArgumentException("Failed to serialize " + s1 + ": " + s);
                    });

                    optional1 = Optional.of(nbtbase);
                }

                list.add(new RegistrySynchronization.a(holder_c.key().location(), optional1));
            });
            biconsumer.accept(iregistry.key(), list);
        });
    }

    private static Stream<IRegistryCustom.d<?>> ownedNetworkableRegistries(IRegistryCustom iregistrycustom) {
        return iregistrycustom.registries().filter((iregistrycustom_d) -> {
            return RegistrySynchronization.NETWORKABLE_REGISTRIES.contains(iregistrycustom_d.key());
        });
    }

    public static Stream<IRegistryCustom.d<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess) {
        return ownedNetworkableRegistries(layeredregistryaccess.getAccessFrom(RegistryLayer.WORLDGEN));
    }

    public static Stream<IRegistryCustom.d<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess) {
        Stream<IRegistryCustom.d<?>> stream = layeredregistryaccess.getLayer(RegistryLayer.STATIC).registries();
        Stream<IRegistryCustom.d<?>> stream1 = networkedRegistries(layeredregistryaccess);

        return Stream.concat(stream1, stream);
    }

    public static record a(MinecraftKey id, Optional<NBTBase> data) {

        public static final StreamCodec<ByteBuf, RegistrySynchronization.a> STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, RegistrySynchronization.a::id, ByteBufCodecs.TAG.apply(ByteBufCodecs::optional), RegistrySynchronization.a::data, RegistrySynchronization.a::new);
    }
}
