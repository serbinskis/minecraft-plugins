package net.minecraft.network.protocol.common;

import java.util.Map;
import net.minecraft.core.IRegistry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public class ClientboundUpdateTagsPacket implements Packet<ClientCommonPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ClientboundUpdateTagsPacket> STREAM_CODEC = Packet.codec(ClientboundUpdateTagsPacket::write, ClientboundUpdateTagsPacket::new);
    private final Map<ResourceKey<? extends IRegistry<?>>, TagNetworkSerialization.a> tags;

    public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends IRegistry<?>>, TagNetworkSerialization.a> map) {
        this.tags = map;
    }

    private ClientboundUpdateTagsPacket(PacketDataSerializer packetdataserializer) {
        this.tags = packetdataserializer.readMap(PacketDataSerializer::readRegistryKey, TagNetworkSerialization.a::read);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeMap(this.tags, PacketDataSerializer::writeResourceKey, (packetdataserializer1, tagnetworkserialization_a) -> {
            tagnetworkserialization_a.write(packetdataserializer1);
        });
    }

    @Override
    public PacketType<ClientboundUpdateTagsPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_UPDATE_TAGS;
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleUpdateTags(this);
    }

    public Map<ResourceKey<? extends IRegistry<?>>, TagNetworkSerialization.a> getTags() {
        return this.tags;
    }
}
