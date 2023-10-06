package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.status.PacketStatusInPing;

public interface ServerPingPacketListener extends PacketListener {

    void handlePingRequest(PacketStatusInPing packetstatusinping);
}
