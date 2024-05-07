package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInTileNBTQuery implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInTileNBTQuery> STREAM_CODEC = Packet.codec(PacketPlayInTileNBTQuery::write, PacketPlayInTileNBTQuery::new);
    private final int transactionId;
    private final BlockPosition pos;

    public PacketPlayInTileNBTQuery(int i, BlockPosition blockposition) {
        this.transactionId = i;
        this.pos = blockposition;
    }

    private PacketPlayInTileNBTQuery(PacketDataSerializer packetdataserializer) {
        this.transactionId = packetdataserializer.readVarInt();
        this.pos = packetdataserializer.readBlockPos();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.transactionId);
        packetdataserializer.writeBlockPos(this.pos);
    }

    @Override
    public PacketType<PacketPlayInTileNBTQuery> type() {
        return GamePacketTypes.SERVERBOUND_BLOCK_ENTITY_TAG_QUERY;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleBlockEntityTagQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public BlockPosition getPos() {
        return this.pos;
    }
}
