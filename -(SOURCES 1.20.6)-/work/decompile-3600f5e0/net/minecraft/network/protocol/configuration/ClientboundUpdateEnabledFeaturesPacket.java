package net.minecraft.network.protocol.configuration;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public record ClientboundUpdateEnabledFeaturesPacket(Set<MinecraftKey> features) implements Packet<ClientConfigurationPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ClientboundUpdateEnabledFeaturesPacket> STREAM_CODEC = Packet.codec(ClientboundUpdateEnabledFeaturesPacket::write, ClientboundUpdateEnabledFeaturesPacket::new);

    private ClientboundUpdateEnabledFeaturesPacket(PacketDataSerializer packetdataserializer) {
        this((Set) packetdataserializer.readCollection(HashSet::new, PacketDataSerializer::readResourceLocation));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.features, PacketDataSerializer::writeResourceLocation);
    }

    @Override
    public PacketType<ClientboundUpdateEnabledFeaturesPacket> type() {
        return ConfigurationPacketTypes.CLIENTBOUND_UPDATE_ENABLED_FEATURES;
    }

    public void handle(ClientConfigurationPacketListener clientconfigurationpacketlistener) {
        clientconfigurationpacketlistener.handleEnabledFeatures(this);
    }
}
