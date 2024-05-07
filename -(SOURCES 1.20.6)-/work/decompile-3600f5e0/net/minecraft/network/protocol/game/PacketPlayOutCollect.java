package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutCollect implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutCollect> STREAM_CODEC = Packet.codec(PacketPlayOutCollect::write, PacketPlayOutCollect::new);
    private final int itemId;
    private final int playerId;
    private final int amount;

    public PacketPlayOutCollect(int i, int j, int k) {
        this.itemId = i;
        this.playerId = j;
        this.amount = k;
    }

    private PacketPlayOutCollect(PacketDataSerializer packetdataserializer) {
        this.itemId = packetdataserializer.readVarInt();
        this.playerId = packetdataserializer.readVarInt();
        this.amount = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.itemId);
        packetdataserializer.writeVarInt(this.playerId);
        packetdataserializer.writeVarInt(this.amount);
    }

    @Override
    public PacketType<PacketPlayOutCollect> type() {
        return GamePacketTypes.CLIENTBOUND_TAKE_ITEM_ENTITY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTakeItemEntity(this);
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public int getAmount() {
        return this.amount;
    }
}
