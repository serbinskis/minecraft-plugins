package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutHeldItemSlot implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutHeldItemSlot> STREAM_CODEC = Packet.codec(PacketPlayOutHeldItemSlot::write, PacketPlayOutHeldItemSlot::new);
    private final int slot;

    public PacketPlayOutHeldItemSlot(int i) {
        this.slot = i;
    }

    private PacketPlayOutHeldItemSlot(PacketDataSerializer packetdataserializer) {
        this.slot = packetdataserializer.readByte();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.slot);
    }

    @Override
    public PacketType<PacketPlayOutHeldItemSlot> type() {
        return GamePacketTypes.CLIENTBOUND_SET_CARRIED_ITEM;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}
