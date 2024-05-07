package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record RaidsDebugPayload(List<BlockPosition> raidCenters) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, RaidsDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(RaidsDebugPayload::write, RaidsDebugPayload::new);
    public static final CustomPacketPayload.b<RaidsDebugPayload> TYPE = CustomPacketPayload.createType("debug/raids");

    private RaidsDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readList(BlockPosition.STREAM_CODEC));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.raidCenters, BlockPosition.STREAM_CODEC);
    }

    @Override
    public CustomPacketPayload.b<RaidsDebugPayload> type() {
        return RaidsDebugPayload.TYPE;
    }
}
