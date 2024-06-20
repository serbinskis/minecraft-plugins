package net.minecraft.network.protocol.common.custom;

import java.util.UUID;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;

public record BreezeDebugPayload(BreezeDebugPayload.a breezeInfo) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, BreezeDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(BreezeDebugPayload::write, BreezeDebugPayload::new);
    public static final CustomPacketPayload.b<BreezeDebugPayload> TYPE = CustomPacketPayload.createType("debug/breeze");

    private BreezeDebugPayload(PacketDataSerializer packetdataserializer) {
        this(new BreezeDebugPayload.a(packetdataserializer));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        this.breezeInfo.write(packetdataserializer);
    }

    @Override
    public CustomPacketPayload.b<BreezeDebugPayload> type() {
        return BreezeDebugPayload.TYPE;
    }

    public static record a(UUID uuid, int id, Integer attackTarget, BlockPosition jumpTarget) {

        public a(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readUUID(), packetdataserializer.readInt(), (Integer) packetdataserializer.readNullable(PacketDataSerializer::readInt), (BlockPosition) packetdataserializer.readNullable(BlockPosition.STREAM_CODEC));
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeUUID(this.uuid);
            packetdataserializer.writeInt(this.id);
            packetdataserializer.writeNullable(this.attackTarget, PacketDataSerializer::writeInt);
            packetdataserializer.writeNullable(this.jumpTarget, BlockPosition.STREAM_CODEC);
        }

        public String generateName() {
            return DebugEntityNameGenerator.getEntityName(this.uuid);
        }

        public String toString() {
            return this.generateName();
        }
    }
}
