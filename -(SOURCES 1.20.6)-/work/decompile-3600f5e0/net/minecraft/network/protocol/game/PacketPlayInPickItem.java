package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInPickItem implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInPickItem> STREAM_CODEC = Packet.codec(PacketPlayInPickItem::write, PacketPlayInPickItem::new);
    private final int slot;

    public PacketPlayInPickItem(int i) {
        this.slot = i;
    }

    private PacketPlayInPickItem(PacketDataSerializer packetdataserializer) {
        this.slot = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.slot);
    }

    @Override
    public PacketType<PacketPlayInPickItem> type() {
        return GamePacketTypes.SERVERBOUND_PICK_ITEM;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePickItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}
