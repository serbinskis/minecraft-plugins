package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.World;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;

public record StructuresDebugPayload(ResourceKey<World> dimension, StructureBoundingBox mainBB, List<StructuresDebugPayload.a> pieces) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/structures");

    public StructuresDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readResourceKey(Registries.DIMENSION), readBoundingBox(packetdataserializer), packetdataserializer.readList(StructuresDebugPayload.a::new));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceKey(this.dimension);
        writeBoundingBox(packetdataserializer, this.mainBB);
        packetdataserializer.writeCollection(this.pieces, (packetdataserializer1, structuresdebugpayload_a) -> {
            structuresdebugpayload_a.write(packetdataserializer);
        });
    }

    @Override
    public MinecraftKey id() {
        return StructuresDebugPayload.ID;
    }

    static StructureBoundingBox readBoundingBox(PacketDataSerializer packetdataserializer) {
        return new StructureBoundingBox(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt());
    }

    static void writeBoundingBox(PacketDataSerializer packetdataserializer, StructureBoundingBox structureboundingbox) {
        packetdataserializer.writeInt(structureboundingbox.minX());
        packetdataserializer.writeInt(structureboundingbox.minY());
        packetdataserializer.writeInt(structureboundingbox.minZ());
        packetdataserializer.writeInt(structureboundingbox.maxX());
        packetdataserializer.writeInt(structureboundingbox.maxY());
        packetdataserializer.writeInt(structureboundingbox.maxZ());
    }

    public static record a(StructureBoundingBox boundingBox, boolean isStart) {

        public a(PacketDataSerializer packetdataserializer) {
            this(StructuresDebugPayload.readBoundingBox(packetdataserializer), packetdataserializer.readBoolean());
        }

        public void write(PacketDataSerializer packetdataserializer) {
            StructuresDebugPayload.writeBoundingBox(packetdataserializer, this.boundingBox);
            packetdataserializer.writeBoolean(this.isStart);
        }
    }
}
