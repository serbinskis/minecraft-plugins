package net.minecraft.network.protocol.status;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.network.protocol.ping.ServerPingPacketListener;

public interface PacketStatusInListener extends ServerPacketListener, ServerPingPacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.STATUS;
    }

    void handleStatusRequest(PacketStatusInStart packetstatusinstart);
}
