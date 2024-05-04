package net.minecraft.network.protocol.status;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;

public class StatusProtocols {

    public static final ProtocolInfo<PacketStatusInListener> SERVERBOUND = ProtocolInfoBuilder.serverboundProtocol(EnumProtocol.STATUS, (protocolinfobuilder) -> {
        protocolinfobuilder.addPacket(StatusPacketTypes.SERVERBOUND_STATUS_REQUEST, PacketStatusInStart.STREAM_CODEC).addPacket(PingPacketTypes.SERVERBOUND_PING_REQUEST, ServerboundPingRequestPacket.STREAM_CODEC);
    });
    public static final ProtocolInfo<PacketStatusOutListener> CLIENTBOUND = ProtocolInfoBuilder.clientboundProtocol(EnumProtocol.STATUS, (protocolinfobuilder) -> {
        protocolinfobuilder.addPacket(StatusPacketTypes.CLIENTBOUND_STATUS_RESPONSE, PacketStatusOutServerInfo.STREAM_CODEC).addPacket(PingPacketTypes.CLIENTBOUND_PONG_RESPONSE, ClientboundPongResponsePacket.STREAM_CODEC);
    });

    public StatusProtocols() {}
}
