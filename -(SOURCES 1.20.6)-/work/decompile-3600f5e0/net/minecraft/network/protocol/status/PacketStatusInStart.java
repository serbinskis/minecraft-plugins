package net.minecraft.network.protocol.status;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketStatusInStart implements Packet<PacketStatusInListener> {

    public static final PacketStatusInStart INSTANCE = new PacketStatusInStart();
    public static final StreamCodec<ByteBuf, PacketStatusInStart> STREAM_CODEC = StreamCodec.unit(PacketStatusInStart.INSTANCE);

    private PacketStatusInStart() {}

    @Override
    public PacketType<PacketStatusInStart> type() {
        return StatusPacketTypes.SERVERBOUND_STATUS_REQUEST;
    }

    public void handle(PacketStatusInListener packetstatusinlistener) {
        packetstatusinlistener.handleStatusRequest(this);
    }
}
