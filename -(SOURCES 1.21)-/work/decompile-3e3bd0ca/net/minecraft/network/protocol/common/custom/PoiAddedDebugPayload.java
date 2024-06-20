package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record PoiAddedDebugPayload(BlockPosition pos, String poiType, int freeTicketCount) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, PoiAddedDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(PoiAddedDebugPayload::write, PoiAddedDebugPayload::new);
    public static final CustomPacketPayload.b<PoiAddedDebugPayload> TYPE = CustomPacketPayload.createType("debug/poi_added");

    private PoiAddedDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos(), packetdataserializer.readUtf(), packetdataserializer.readInt());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeUtf(this.poiType);
        packetdataserializer.writeInt(this.freeTicketCount);
    }

    @Override
    public CustomPacketPayload.b<PoiAddedDebugPayload> type() {
        return PoiAddedDebugPayload.TYPE;
    }
}
