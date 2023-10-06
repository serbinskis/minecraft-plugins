package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ClientboundChunkBatchStartPacket() implements Packet<PacketListenerPlayOut> {

    public ClientboundChunkBatchStartPacket(PacketDataSerializer packetdataserializer) {
        this();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleChunkBatchStart(this);
    }
}
