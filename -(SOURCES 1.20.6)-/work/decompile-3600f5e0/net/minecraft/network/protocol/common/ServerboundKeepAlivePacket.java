package net.minecraft.network.protocol.common;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundKeepAlivePacket implements Packet<ServerCommonPacketListener> {

    public static final StreamCodec<PacketDataSerializer, ServerboundKeepAlivePacket> STREAM_CODEC = Packet.codec(ServerboundKeepAlivePacket::write, ServerboundKeepAlivePacket::new);
    private final long id;

    public ServerboundKeepAlivePacket(long i) {
        this.id = i;
    }

    private ServerboundKeepAlivePacket(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readLong();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLong(this.id);
    }

    @Override
    public PacketType<ServerboundKeepAlivePacket> type() {
        return CommonPacketTypes.SERVERBOUND_KEEP_ALIVE;
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handleKeepAlive(this);
    }

    public long getId() {
        return this.id;
    }
}
