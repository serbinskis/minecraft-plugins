package net.minecraft.network.protocol.game;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ClientboundStartConfigurationPacket() implements Packet<PacketListenerPlayOut> {

    public ClientboundStartConfigurationPacket(PacketDataSerializer packetdataserializer) {
        this();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleConfigurationStart(this);
    }

    @Override
    public EnumProtocol nextProtocol() {
        return EnumProtocol.CONFIGURATION;
    }
}
