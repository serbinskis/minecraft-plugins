package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.pathfinder.PathEntity;

public record PathfindingDebugPayload(int entityId, PathEntity path, float maxNodeDistance) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/path");

    public PathfindingDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readInt(), PathEntity.createFromStream(packetdataserializer), packetdataserializer.readFloat());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.entityId);
        this.path.writeToStream(packetdataserializer);
        packetdataserializer.writeFloat(this.maxNodeDistance);
    }

    @Override
    public MinecraftKey id() {
        return PathfindingDebugPayload.ID;
    }
}
