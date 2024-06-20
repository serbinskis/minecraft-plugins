package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record NeighborUpdatesDebugPayload(long time, BlockPosition pos) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, NeighborUpdatesDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(NeighborUpdatesDebugPayload::write, NeighborUpdatesDebugPayload::new);
    public static final CustomPacketPayload.b<NeighborUpdatesDebugPayload> TYPE = CustomPacketPayload.createType("debug/neighbors_update");

    private NeighborUpdatesDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarLong(), packetdataserializer.readBlockPos());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarLong(this.time);
        packetdataserializer.writeBlockPos(this.pos);
    }

    @Override
    public CustomPacketPayload.b<NeighborUpdatesDebugPayload> type() {
        return NeighborUpdatesDebugPayload.TYPE;
    }
}
