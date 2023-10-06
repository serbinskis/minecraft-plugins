package net.minecraft.network.protocol.status;

import net.minecraft.network.ClientPongPacketListener;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.EnumProtocol;

public interface PacketStatusOutListener extends ClientPongPacketListener, ClientboundPacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.STATUS;
    }

    void handleStatusResponse(PacketStatusOutServerInfo packetstatusoutserverinfo);
}
