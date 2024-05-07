package net.minecraft.network.protocol.handshake;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;

public class HandshakeProtocols {

    public static final ProtocolInfo<PacketHandshakingInListener> SERVERBOUND = ProtocolInfoBuilder.serverboundProtocol(EnumProtocol.HANDSHAKING, (protocolinfobuilder) -> {
        protocolinfobuilder.addPacket(HandshakePacketTypes.CLIENT_INTENTION, PacketHandshakingInSetProtocol.STREAM_CODEC);
    });

    public HandshakeProtocols() {}
}
