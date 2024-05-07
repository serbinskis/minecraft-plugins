package net.minecraft.network.protocol.status;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketStatusOutServerInfo(ServerPing status) implements Packet<PacketStatusOutListener> {

    public static final StreamCodec<PacketDataSerializer, PacketStatusOutServerInfo> STREAM_CODEC = Packet.codec(PacketStatusOutServerInfo::write, PacketStatusOutServerInfo::new);

    private PacketStatusOutServerInfo(PacketDataSerializer packetdataserializer) {
        this((ServerPing) packetdataserializer.readJsonWithCodec(ServerPing.CODEC));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeJsonWithCodec(ServerPing.CODEC, this.status);
    }

    @Override
    public PacketType<PacketStatusOutServerInfo> type() {
        return StatusPacketTypes.CLIENTBOUND_STATUS_RESPONSE;
    }

    public void handle(PacketStatusOutListener packetstatusoutlistener) {
        packetstatusoutlistener.handleStatusResponse(this);
    }
}
