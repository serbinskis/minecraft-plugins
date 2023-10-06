package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record WorldGenAttemptDebugPayload(BlockPosition pos, float scale, float red, float green, float blue, float alpha) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/worldgen_attempt");

    public WorldGenAttemptDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readBlockPos(), packetdataserializer.readFloat(), packetdataserializer.readFloat(), packetdataserializer.readFloat(), packetdataserializer.readFloat(), packetdataserializer.readFloat());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeFloat(this.scale);
        packetdataserializer.writeFloat(this.red);
        packetdataserializer.writeFloat(this.green);
        packetdataserializer.writeFloat(this.blue);
        packetdataserializer.writeFloat(this.alpha);
    }

    @Override
    public MinecraftKey id() {
        return WorldGenAttemptDebugPayload.ID;
    }
}
