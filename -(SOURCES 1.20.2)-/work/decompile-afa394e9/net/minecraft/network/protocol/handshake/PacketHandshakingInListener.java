package net.minecraft.network.protocol.handshake;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;

public interface PacketHandshakingInListener extends ServerPacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.HANDSHAKING;
    }

    void handleIntention(PacketHandshakingInSetProtocol packethandshakinginsetprotocol);
}
