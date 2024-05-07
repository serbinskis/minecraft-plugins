package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundPingPacket implements Packet<ClientCommonPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ClientboundPingPacket> STREAM_CODEC = Packet.codec(ClientboundPingPacket::write, ClientboundPingPacket::new);
    private final int id;

    public ClientboundPingPacket(int i) {
        this.id = i;
    }

    private ClientboundPingPacket(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.id);
    }

    @Override
    public PacketType<ClientboundPingPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_PING;
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handlePing(this);
    }

    public int getId() {
        return this.id;
    }
}
