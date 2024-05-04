package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInHeldItemSlot implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInHeldItemSlot> STREAM_CODEC = Packet.codec(PacketPlayInHeldItemSlot::write, PacketPlayInHeldItemSlot::new);
    private final int slot;

    public PacketPlayInHeldItemSlot(int i) {
        this.slot = i;
    }

    private PacketPlayInHeldItemSlot(PacketDataSerializer packetdataserializer) {
        this.slot = packetdataserializer.readShort();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeShort(this.slot);
    }

    @Override
    public PacketType<PacketPlayInHeldItemSlot> type() {
        return GamePacketTypes.SERVERBOUND_SET_CARRIED_ITEM;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}
