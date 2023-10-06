package net.minecraft.network;

import net.minecraft.network.protocol.EnumProtocolDirection;

public interface ClientboundPacketListener extends PacketListener {

    @Override
    default EnumProtocolDirection flow() {
        return EnumProtocolDirection.CLIENTBOUND;
    }
}
