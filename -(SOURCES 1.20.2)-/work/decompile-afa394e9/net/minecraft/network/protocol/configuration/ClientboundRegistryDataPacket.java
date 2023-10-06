package net.minecraft.network.protocol.configuration;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.RegistryOps;

public record ClientboundRegistryDataPacket(IRegistryCustom.Dimension registryHolder) implements Packet<ClientConfigurationPacketListener> {

    private static final RegistryOps<NBTBase> BUILTIN_CONTEXT_OPS = RegistryOps.create(DynamicOpsNBT.INSTANCE, (HolderLookup.b) IRegistryCustom.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

    public ClientboundRegistryDataPacket(PacketDataSerializer packetdataserializer) {
        this(((IRegistryCustom) packetdataserializer.readWithCodecTrusted(ClientboundRegistryDataPacket.BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC)).freeze());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeWithCodec(ClientboundRegistryDataPacket.BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC, this.registryHolder);
    }

    public void handle(ClientConfigurationPacketListener clientconfigurationpacketlistener) {
        clientconfigurationpacketlistener.handleRegistryData(this);
    }
}
