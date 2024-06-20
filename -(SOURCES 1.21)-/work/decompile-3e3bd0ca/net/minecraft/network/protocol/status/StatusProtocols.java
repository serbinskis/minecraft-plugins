package net.minecraft.network.protocol.status;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;

public class StatusProtocols {

    public static final ProtocolInfo.a<PacketStatusInListener, ByteBuf> SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(EnumProtocol.STATUS, (protocolinfobuilder) -> {
        protocolinfobuilder.addPacket(StatusPacketTypes.SERVERBOUND_STATUS_REQUEST, PacketStatusInStart.STREAM_CODEC).addPacket(PingPacketTypes.SERVERBOUND_PING_REQUEST, ServerboundPingRequestPacket.STREAM_CODEC);
    });
    public static final ProtocolInfo<PacketStatusInListener> SERVERBOUND = StatusProtocols.SERVERBOUND_TEMPLATE.bind((bytebuf) -> {
        return bytebuf;
    });
    public static final ProtocolInfo.a<PacketStatusOutListener, PacketDataSerializer> CLIENTBOUND_TEMPLATE = ProtocolInfoBuilder.clientboundProtocol(EnumProtocol.STATUS, (protocolinfobuilder) -> {
        protocolinfobuilder.addPacket(StatusPacketTypes.CLIENTBOUND_STATUS_RESPONSE, PacketStatusOutServerInfo.STREAM_CODEC).addPacket(PingPacketTypes.CLIENTBOUND_PONG_RESPONSE, ClientboundPongResponsePacket.STREAM_CODEC);
    });
    public static final ProtocolInfo<PacketStatusOutListener> CLIENTBOUND = StatusProtocols.CLIENTBOUND_TEMPLATE.bind(PacketDataSerializer::new);

    public StatusProtocols() {}
}
