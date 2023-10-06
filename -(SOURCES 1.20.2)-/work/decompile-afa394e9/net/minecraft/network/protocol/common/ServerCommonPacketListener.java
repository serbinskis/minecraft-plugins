package net.minecraft.network.protocol.common;

import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerCommonPacketListener extends ServerPacketListener {

    void handleKeepAlive(ServerboundKeepAlivePacket serverboundkeepalivepacket);

    void handlePong(ServerboundPongPacket serverboundpongpacket);

    void handleCustomPayload(ServerboundCustomPayloadPacket serverboundcustompayloadpacket);

    void handleResourcePackResponse(ServerboundResourcePackPacket serverboundresourcepackpacket);

    void handleClientInformation(ServerboundClientInformationPacket serverboundclientinformationpacket);
}
