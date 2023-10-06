package net.minecraft.network.protocol.handshake;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record PacketHandshakingInSetProtocol(int protocolVersion, String hostName, int port, ClientIntent intention) implements Packet<PacketHandshakingInListener> {

    private static final int MAX_HOST_LENGTH = 255;

    public PacketHandshakingInSetProtocol(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt(), packetdataserializer.readUtf(255), packetdataserializer.readUnsignedShort(), ClientIntent.byId(packetdataserializer.readVarInt()));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.protocolVersion);
        packetdataserializer.writeUtf(this.hostName);
        packetdataserializer.writeShort(this.port);
        packetdataserializer.writeVarInt(this.intention.id());
    }

    public void handle(PacketHandshakingInListener packethandshakinginlistener) {
        packethandshakinginlistener.handleIntention(this);
    }

    @Override
    public EnumProtocol nextProtocol() {
        return this.intention.protocol();
    }
}
