package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.pathfinder.PathEntity;

public record PathfindingDebugPayload(int entityId, PathEntity path, float maxNodeDistance) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, PathfindingDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(PathfindingDebugPayload::write, PathfindingDebugPayload::new);
    public static final CustomPacketPayload.b<PathfindingDebugPayload> TYPE = CustomPacketPayload.createType("debug/path");

    private PathfindingDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readInt(), PathEntity.createFromStream(packetdataserializer), packetdataserializer.readFloat());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.entityId);
        this.path.writeToStream(packetdataserializer);
        packetdataserializer.writeFloat(this.maxNodeDistance);
    }

    @Override
    public CustomPacketPayload.b<PathfindingDebugPayload> type() {
        return PathfindingDebugPayload.TYPE;
    }
}
