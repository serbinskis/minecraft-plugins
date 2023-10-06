package net.minecraft.network.protocol.game;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ServerboundConfigurationAcknowledgedPacket() implements Packet<PacketListenerPlayIn> {

    public ServerboundConfigurationAcknowledgedPacket(PacketDataSerializer packetdataserializer) {
        this();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleConfigurationAcknowledged(this);
    }

    @Override
    public EnumProtocol nextProtocol() {
        return EnumProtocol.CONFIGURATION;
    }
}
