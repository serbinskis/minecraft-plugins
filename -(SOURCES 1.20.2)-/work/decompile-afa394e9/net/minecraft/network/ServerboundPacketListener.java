package net.minecraft.network;

import net.minecraft.network.protocol.EnumProtocolDirection;

public interface ServerboundPacketListener extends PacketListener {

    @Override
    default EnumProtocolDirection flow() {
        return EnumProtocolDirection.SERVERBOUND;
    }
}
