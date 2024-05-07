package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record VillageSectionsDebugPayload(Set<SectionPosition> villageChunks, Set<SectionPosition> notVillageChunks) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, VillageSectionsDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(VillageSectionsDebugPayload::write, VillageSectionsDebugPayload::new);
    public static final CustomPacketPayload.b<VillageSectionsDebugPayload> TYPE = CustomPacketPayload.createType("debug/village_sections");

    private VillageSectionsDebugPayload(PacketDataSerializer packetdataserializer) {
        this((Set) packetdataserializer.readCollection(HashSet::new, PacketDataSerializer::readSectionPos), (Set) packetdataserializer.readCollection(HashSet::new, PacketDataSerializer::readSectionPos));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.villageChunks, PacketDataSerializer::writeSectionPos);
        packetdataserializer.writeCollection(this.notVillageChunks, PacketDataSerializer::writeSectionPos);
    }

    @Override
    public CustomPacketPayload.b<VillageSectionsDebugPayload> type() {
        return VillageSectionsDebugPayload.TYPE;
    }
}
