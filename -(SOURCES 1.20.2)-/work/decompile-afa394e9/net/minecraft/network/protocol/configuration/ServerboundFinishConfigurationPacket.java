package net.minecraft.network.protocol.configuration;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ServerboundFinishConfigurationPacket() implements Packet<ServerConfigurationPacketListener> {

    public ServerboundFinishConfigurationPacket(PacketDataSerializer packetdataserializer) {
        this();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}

    public void handle(ServerConfigurationPacketListener serverconfigurationpacketlistener) {
        serverconfigurationpacketlistener.handleConfigurationFinished(this);
    }

    @Override
    public EnumProtocol nextProtocol() {
        return EnumProtocol.PLAY;
    }
}
