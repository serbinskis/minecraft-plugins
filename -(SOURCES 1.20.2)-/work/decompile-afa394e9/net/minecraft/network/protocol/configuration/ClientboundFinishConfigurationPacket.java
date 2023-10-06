package net.minecraft.network.protocol.configuration;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ClientboundFinishConfigurationPacket() implements Packet<ClientConfigurationPacketListener> {

    public ClientboundFinishConfigurationPacket(PacketDataSerializer packetdataserializer) {
        this();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}

    public void handle(ClientConfigurationPacketListener clientconfigurationpacketlistener) {
        clientconfigurationpacketlistener.handleConfigurationFinished(this);
    }

    @Override
    public EnumProtocol nextProtocol() {
        return EnumProtocol.PLAY;
    }
}
