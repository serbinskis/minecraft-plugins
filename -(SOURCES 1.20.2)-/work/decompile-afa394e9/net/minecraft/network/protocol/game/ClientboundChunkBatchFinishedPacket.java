package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ClientboundChunkBatchFinishedPacket(int batchSize) implements Packet<PacketListenerPlayOut> {

    public ClientboundChunkBatchFinishedPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.batchSize);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleChunkBatchFinished(this);
    }
}
