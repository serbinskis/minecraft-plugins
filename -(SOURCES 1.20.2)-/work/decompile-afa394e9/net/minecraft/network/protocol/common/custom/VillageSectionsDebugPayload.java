package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record VillageSectionsDebugPayload(Set<SectionPosition> villageChunks, Set<SectionPosition> notVillageChunks) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/village_sections");

    public VillageSectionsDebugPayload(PacketDataSerializer packetdataserializer) {
        this((Set) packetdataserializer.readCollection(HashSet::new, PacketDataSerializer::readSectionPos), (Set) packetdataserializer.readCollection(HashSet::new, PacketDataSerializer::readSectionPos));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.villageChunks, PacketDataSerializer::writeSectionPos);
        packetdataserializer.writeCollection(this.notVillageChunks, PacketDataSerializer::writeSectionPos);
    }

    @Override
    public MinecraftKey id() {
        return VillageSectionsDebugPayload.ID;
    }
}
