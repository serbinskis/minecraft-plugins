package net.minecraft.network.protocol.common;

import java.util.Map;
import net.minecraft.core.IRegistry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public class ClientboundUpdateTagsPacket implements Packet<ClientCommonPacketListener> {

    private final Map<ResourceKey<? extends IRegistry<?>>, TagNetworkSerialization.a> tags;

    public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends IRegistry<?>>, TagNetworkSerialization.a> map) {
        this.tags = map;
    }

    public ClientboundUpdateTagsPacket(PacketDataSerializer packetdataserializer) {
        this.tags = packetdataserializer.readMap(PacketDataSerializer::readRegistryKey, TagNetworkSerialization.a::read);
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeMap(this.tags, PacketDataSerializer::writeResourceKey, (packetdataserializer1, tagnetworkserialization_a) -> {
            tagnetworkserialization_a.write(packetdataserializer1);
        });
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleUpdateTags(this);
    }

    public Map<ResourceKey<? extends IRegistry<?>>, TagNetworkSerialization.a> getTags() {
        return this.tags;
    }
}
