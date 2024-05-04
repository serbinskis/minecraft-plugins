package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundKeepAlivePacket implements Packet<ClientCommonPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ClientboundKeepAlivePacket> STREAM_CODEC = Packet.codec(ClientboundKeepAlivePacket::write, ClientboundKeepAlivePacket::new);
    private final long id;

    public ClientboundKeepAlivePacket(long i) {
        this.id = i;
    }

    private ClientboundKeepAlivePacket(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readLong();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.id);
    }

    @Override
    public PacketType<ClientboundKeepAlivePacket> type() {
        return CommonPacketTypes.CLIENTBOUND_KEEP_ALIVE;
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleKeepAlive(this);
    }

    public long getId() {
        return this.id;
    }
}
