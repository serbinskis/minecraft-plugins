package net.minecraft.network;

import net.minecraft.network.protocol.status.PacketStatusOutPong;

public interface ClientPongPacketListener extends PacketListener {

    void handlePongResponse(PacketStatusOutPong packetstatusoutpong);
}
