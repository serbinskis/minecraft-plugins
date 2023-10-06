package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record PoiTicketCountDebugPayload(BlockPosition pos, int freeTicketCount) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/poi_ticket_count");

    public PoiTicketCountDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos(), packetdataserializer.readInt());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeInt(this.freeTicketCount);
    }

    @Override
    public MinecraftKey id() {
        return PoiTicketCountDebugPayload.ID;
    }
}
