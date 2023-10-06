package net.minecraft.network.protocol.configuration;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.MinecraftKey;

public record ClientboundUpdateEnabledFeaturesPacket(Set<MinecraftKey> features) implements Packet<ClientConfigurationPacketListener> {

    public ClientboundUpdateEnabledFeaturesPacket(PacketDataSerializer packetdataserializer) {
        this((Set) packetdataserializer.readCollection(HashSet::new, PacketDataSerializer::readResourceLocation));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.features, PacketDataSerializer::writeResourceLocation);
    }

    public void handle(ClientConfigurationPacketListener clientconfigurationpacketlistener) {
        clientconfigurationpacketlistener.handleEnabledFeatures(this);
    }
}
