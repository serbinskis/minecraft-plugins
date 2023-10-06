package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record GameTestAddMarkerDebugPayload(BlockPosition pos, int color, String text, int durationMs) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/game_test_add_marker");

    public GameTestAddMarkerDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos(), packetdataserializer.readInt(), packetdataserializer.readUtf(), packetdataserializer.readInt());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeInt(this.color);
        packetdataserializer.writeUtf(this.text);
        packetdataserializer.writeInt(this.durationMs);
    }

    @Override
    public MinecraftKey id() {
        return GameTestAddMarkerDebugPayload.ID;
    }
}
