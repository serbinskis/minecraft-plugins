package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, ServerboundContainerSlotStateChangedPacket> STREAM_CODEC = Packet.codec(ServerboundContainerSlotStateChangedPacket::write, ServerboundContainerSlotStateChangedPacket::new);

    private ServerboundContainerSlotStateChangedPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt(), packetdataserializer.readVarInt(), packetdataserializer.readBoolean());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.slotId);
        packetdataserializer.writeVarInt(this.containerId);
        packetdataserializer.writeBoolean(this.newState);
    }

    @Override
    public PacketType<ServerboundContainerSlotStateChangedPacket> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_SLOT_STATE_CHANGED;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleContainerSlotStateChanged(this);
    }
}
