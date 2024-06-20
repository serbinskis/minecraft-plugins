package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record GameTestAddMarkerDebugPayload(BlockPosition pos, int color, String text, int durationMs) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, GameTestAddMarkerDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(GameTestAddMarkerDebugPayload::write, GameTestAddMarkerDebugPayload::new);
    public static final CustomPacketPayload.b<GameTestAddMarkerDebugPayload> TYPE = CustomPacketPayload.createType("debug/game_test_add_marker");

    private GameTestAddMarkerDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos(), packetdataserializer.readInt(), packetdataserializer.readUtf(), packetdataserializer.readInt());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeInt(this.color);
        packetdataserializer.writeUtf(this.text);
        packetdataserializer.writeInt(this.durationMs);
    }

    @Override
    public CustomPacketPayload.b<GameTestAddMarkerDebugPayload> type() {
        return GameTestAddMarkerDebugPayload.TYPE;
    }
}
