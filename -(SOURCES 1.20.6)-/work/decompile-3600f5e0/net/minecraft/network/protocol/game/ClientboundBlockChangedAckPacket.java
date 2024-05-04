package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundBlockChangedAckPacket(int sequence) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundBlockChangedAckPacket> STREAM_CODEC = Packet.codec(ClientboundBlockChangedAckPacket::write, ClientboundBlockChangedAckPacket::new);

    private ClientboundBlockChangedAckPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.sequence);
    }

    @Override
    public PacketType<ClientboundBlockChangedAckPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_CHANGED_ACK;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleBlockChangedAck(this);
    }
}
