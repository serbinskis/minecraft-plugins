package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record PoiTicketCountDebugPayload(BlockPosition pos, int freeTicketCount) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, PoiTicketCountDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(PoiTicketCountDebugPayload::write, PoiTicketCountDebugPayload::new);
    public static final CustomPacketPayload.b<PoiTicketCountDebugPayload> TYPE = CustomPacketPayload.createType("debug/poi_ticket_count");

    private PoiTicketCountDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos(), packetdataserializer.readInt());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeInt(this.freeTicketCount);
    }

    @Override
    public CustomPacketPayload.b<PoiTicketCountDebugPayload> type() {
        return PoiTicketCountDebugPayload.TYPE;
    }
}
