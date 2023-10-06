package net.minecraft.network.protocol.login;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ServerboundLoginAcknowledgedPacket() implements Packet<PacketLoginInListener> {

    public ServerboundLoginAcknowledgedPacket(PacketDataSerializer packetdataserializer) {
        this();
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {}

    public void handle(PacketLoginInListener packetlogininlistener) {
        packetlogininlistener.handleLoginAcknowledgement(this);
    }

    @Override
    public EnumProtocol nextProtocol() {
        return EnumProtocol.CONFIGURATION;
    }
}
