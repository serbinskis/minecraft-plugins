package net.minecraft.network.protocol.status;

import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.ping.ClientPongPacketListener;

public interface PacketStatusOutListener extends ClientPongPacketListener, ClientboundPacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.STATUS;
    }

    void handleStatusResponse(PacketStatusOutServerInfo packetstatusoutserverinfo);
}
