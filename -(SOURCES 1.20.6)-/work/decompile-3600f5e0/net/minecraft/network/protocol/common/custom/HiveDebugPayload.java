package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record HiveDebugPayload(HiveDebugPayload.a hiveInfo) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, HiveDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(HiveDebugPayload::write, HiveDebugPayload::new);
    public static final CustomPacketPayload.b<HiveDebugPayload> TYPE = CustomPacketPayload.createType("debug/hive");

    private HiveDebugPayload(PacketDataSerializer packetdataserializer) {
        this(new HiveDebugPayload.a(packetdataserializer));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        this.hiveInfo.write(packetdataserializer);
    }

    @Override
    public CustomPacketPayload.b<HiveDebugPayload> type() {
        return HiveDebugPayload.TYPE;
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
