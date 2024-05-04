package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record WorldGenAttemptDebugPayload(BlockPosition pos, float scale, float red, float green, float blue, float alpha) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, WorldGenAttemptDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(WorldGenAttemptDebugPayload::write, WorldGenAttemptDebugPayload::new);
    public static final CustomPacketPayload.b<WorldGenAttemptDebugPayload> TYPE = CustomPacketPayload.createType("debug/worldgen_attempt");

    private WorldGenAttemptDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos(), packetdataserializer.readFloat(), packetdataserializer.readFloat(), packetdataserializer.readFloat(), packetdataserializer.readFloat(), packetdataserializer.readFloat());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeFloat(this.scale);
        packetdataserializer.writeFloat(this.red);
        packetdataserializer.writeFloat(this.green);
        packetdataserializer.writeFloat(this.blue);
        packetdataserializer.writeFloat(this.alpha);
    }

    @Override
    public CustomPacketPayload.b<WorldGenAttemptDebugPayload> type() {
        return WorldGenAttemptDebugPayload.TYPE;
    }
}
