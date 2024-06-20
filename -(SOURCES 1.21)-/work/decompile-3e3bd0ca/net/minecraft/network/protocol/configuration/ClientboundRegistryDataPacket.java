package net.minecraft.network.protocol.configuration;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.IRegistry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public record ClientboundRegistryDataPacket(ResourceKey<? extends IRegistry<?>> registry, List<RegistrySynchronization.a> entries) implements Packet<ClientConfigurationPacketListener> {

    private static final StreamCodec<ByteBuf, ResourceKey<? extends IRegistry<?>>> REGISTRY_KEY_STREAM_CODEC = MinecraftKey.STREAM_CODEC.map(ResourceKey::createRegistryKey, ResourceKey::location);
    public static final StreamCodec<PacketDataSerializer, ClientboundRegistryDataPacket> STREAM_CODEC = StreamCodec.composite(ClientboundRegistryDataPacket.REGISTRY_KEY_STREAM_CODEC, ClientboundRegistryDataPacket::registry, RegistrySynchronization.a.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundRegistryDataPacket::entries, ClientboundRegistryDataPacket::new);

    @Override
    public PacketType<ClientboundRegistryDataPacket> type() {
        return ConfigurationPacketTypes.CLIENTBOUND_REGISTRY_DATA;
    }

    public void handle(ClientConfigurationPacketListener clientconfigurationpacketlistener) {
        clientconfigurationpacketlistener.handleRegistryData(this);
    }
}
