package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record HiveDebugPayload(HiveDebugPayload.a hiveInfo) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/hive");

    public HiveDebugPayload(PacketDataSerializer packetdataserializer) {
        this(new HiveDebugPayload.a(packetdataserializer));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        this.hiveInfo.write(packetdataserializer);
    }

    @Override
    public MinecraftKey id() {
        return HiveDebugPayload.ID;
    }

    public static record a(BlockPosition pos, String hiveType, int occupantCount, int honeyLevel, boolean sedated) {

        public a(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readBlockPos(), packetdataserializer.readUtf(), packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readBoolean());
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeBlockPos(this.pos);
            packetdataserializer.writeUtf(this.hiveType);
            packetdataserializer.writeInt(this.occupantCount);
            packetdataserializer.writeInt(this.honeyLevel);
            packetdataserializer.writeBoolean(this.sedated);
        }
    }
}
