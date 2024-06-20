package net.minecraft.network.protocol.ping;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPingRequestPacket implements Packet<ServerPingPacketListener> {

    public static final StreamCodec<ByteBuf, ServerboundPingRequestPacket> STREAM_CODEC = Packet.codec(ServerboundPingRequestPacket::write, ServerboundPingRequestPacket::new);
    private final long time;

    public ServerboundPingRequestPacket(long i) {
        this.time = i;
    }

    private ServerboundPingRequestPacket(ByteBuf bytebuf) {
        this.time = bytebuf.readLong();
    }

    private void write(ByteBuf bytebuf) {
        bytebuf.writeLong(this.time);
    }

    @Override
    public PacketType<ServerboundPingRequestPacket> type() {
        return PingPacketTypes.SERVERBOUND_PING_REQUEST;
    }

    public void handle(ServerPingPacketListener serverpingpacketlistener) {
        serverpingpacketlistener.handlePingRequest(this);
    }

    public long getTime() {
        return this.time;
    }
}
