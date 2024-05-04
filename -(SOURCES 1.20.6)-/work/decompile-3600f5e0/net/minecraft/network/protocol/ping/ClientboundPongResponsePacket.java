package net.minecraft.network.protocol.ping;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundPongResponsePacket(long time) implements Packet<ClientPongPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ClientboundPongResponsePacket> STREAM_CODEC = Packet.codec(ClientboundPongResponsePacket::write, ClientboundPongResponsePacket::new);

    private ClientboundPongResponsePacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readLong());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.time);
    }

    @Override
    public PacketType<ClientboundPongResponsePacket> type() {
        return PingPacketTypes.CLIENTBOUND_PONG_RESPONSE;
    }

    public void handle(ClientPongPacketListener clientpongpacketlistener) {
        clientpongpacketlistener.handlePongResponse(this);
    }
}
