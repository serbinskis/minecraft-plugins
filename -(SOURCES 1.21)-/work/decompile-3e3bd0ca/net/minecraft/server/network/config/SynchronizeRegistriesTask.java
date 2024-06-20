package net.minecraft.server.network.config;

import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.tags.TagNetworkSerialization;

public class SynchronizeRegistriesTask implements ConfigurationTask {

    public static final ConfigurationTask.a TYPE = new ConfigurationTask.a("synchronize_registries");
    private final List<KnownPack> requestedPacks;
    private final LayeredRegistryAccess<RegistryLayer> registries;

    public SynchronizeRegistriesTask(List<KnownPack> list, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess) {
        this.requestedPacks = list;
        this.registries = layeredregistryaccess;
    }

    @Override
    public void start(Consumer<Packet<?>> consumer) {
        consumer.accept(new ClientboundSelectKnownPacks(this.requestedPacks));
    }

    private void sendRegistries(Consumer<Packet<?>> consumer, Set<KnownPack> set) {
        DynamicOps<NBTBase> dynamicops = this.registries.compositeAccess().createSerializationContext(DynamicOpsNBT.INSTANCE);

        RegistrySynchronization.packRegistries(dynamicops, this.registries.getAccessFrom(RegistryLayer.WORLDGEN), set, (resourcekey, list) -> {
            consumer.accept(new ClientboundRegistryDataPacket(resourcekey, list));
        });
        consumer.accept(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
    }

    public void handleResponse(List<KnownPack> list, Consumer<Packet<?>> consumer) {
        if (list.equals(this.requestedPacks)) {
            this.sendRegistries(consumer, Set.copyOf(this.requestedPacks));
        } else {
            this.sendRegistries(consumer, Set.of());
        }

    }

    @Override
    public ConfigurationTask.a type() {
        return SynchronizeRegistriesTask.TYPE;
    }
}
