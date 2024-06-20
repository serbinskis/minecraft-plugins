package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record PoiRemovedDebugPayload(BlockPosition pos) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, PoiRemovedDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(PoiRemovedDebugPayload::write, PoiRemovedDebugPayload::new);
    public static final CustomPacketPayload.b<PoiRemovedDebugPayload> TYPE = CustomPacketPayload.createType("debug/poi_removed");

    private PoiRemovedDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
    }

    @Override
    public CustomPacketPayload.b<PoiRemovedDebugPayload> type() {
        return PoiRemovedDebugPayload.TYPE;
    }
}
