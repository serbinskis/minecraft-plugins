package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInEntityNBTQuery implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInEntityNBTQuery> STREAM_CODEC = Packet.codec(PacketPlayInEntityNBTQuery::write, PacketPlayInEntityNBTQuery::new);
    private final int transactionId;
    private final int entityId;

    public PacketPlayInEntityNBTQuery(int i, int j) {
        this.transactionId = i;
        this.entityId = j;
    }

    private PacketPlayInEntityNBTQuery(PacketDataSerializer packetdataserializer) {
        this.transactionId = packetdataserializer.readVarInt();
        this.entityId = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.transactionId);
        packetdataserializer.writeVarInt(this.entityId);
    }

    @Override
    public PacketType<PacketPlayInEntityNBTQuery> type() {
        return GamePacketTypes.SERVERBOUND_ENTITY_TAG_QUERY;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleEntityTagQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public int getEntityId() {
        return this.entityId;
    }
}
