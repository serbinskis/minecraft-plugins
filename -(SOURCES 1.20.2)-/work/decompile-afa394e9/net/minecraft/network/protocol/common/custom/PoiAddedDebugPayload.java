package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record PoiAddedDebugPayload(BlockPosition pos, String type, int freeTicketCount) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/poi_added");

    public PoiAddedDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos(), packetdataserializer.readUtf(), packetdataserializer.readInt());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeUtf(this.type);
        packetdataserializer.writeInt(this.freeTicketCount);
    }

    @Override
    public MinecraftKey id() {
        return PoiAddedDebugPayload.ID;
    }
}
