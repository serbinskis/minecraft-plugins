package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record GameTestClearMarkersDebugPayload() implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/game_test_clear");

    public GameTestClearMarkersDebugPayload(PacketDataSerializer packetdataserializer) {
        this();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}

    @Override
    public MinecraftKey id() {
        return GameTestClearMarkersDebugPayload.ID;
    }
}
