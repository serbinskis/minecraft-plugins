package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record GameTestClearMarkersDebugPayload() implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, GameTestClearMarkersDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(GameTestClearMarkersDebugPayload::write, GameTestClearMarkersDebugPayload::new);
    public static final CustomPacketPayload.b<GameTestClearMarkersDebugPayload> TYPE = CustomPacketPayload.createType("debug/game_test_clear");

    private GameTestClearMarkersDebugPayload(PacketDataSerializer packetdataserializer) {
        this();
    }

    private void write(PacketDataSerializer packetdataserializer) {}

    @Override
    public CustomPacketPayload.b<GameTestClearMarkersDebugPayload> type() {
        return GameTestClearMarkersDebugPayload.TYPE;
    }
}
