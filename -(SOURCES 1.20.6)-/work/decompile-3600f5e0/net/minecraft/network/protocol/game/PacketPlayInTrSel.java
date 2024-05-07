package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInTrSel implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInTrSel> STREAM_CODEC = Packet.codec(PacketPlayInTrSel::write, PacketPlayInTrSel::new);
    private final int item;

    public PacketPlayInTrSel(int i) {
        this.item = i;
    }

    private PacketPlayInTrSel(PacketDataSerializer packetdataserializer) {
        this.item = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.item);
    }

    @Override
    public PacketType<PacketPlayInTrSel> type() {
        return GamePacketTypes.SERVERBOUND_SELECT_TRADE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSelectTrade(this);
    }

    public int getItem() {
        return this.item;
    }
}
