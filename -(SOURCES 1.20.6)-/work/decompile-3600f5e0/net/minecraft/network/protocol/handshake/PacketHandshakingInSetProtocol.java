package net.minecraft.network.protocol.handshake;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketHandshakingInSetProtocol(int protocolVersion, String hostName, int port, ClientIntent intention) implements Packet<PacketHandshakingInListener> {

    public static final StreamCodec<PacketDataSerializer, PacketHandshakingInSetProtocol> STREAM_CODEC = Packet.codec(PacketHandshakingInSetProtocol::write, PacketHandshakingInSetProtocol::new);
    private static final int MAX_HOST_LENGTH = 255;

    private PacketHandshakingInSetProtocol(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt(), packetdataserializer.readUtf(255), packetdataserializer.readUnsignedShort(), ClientIntent.byId(packetdataserializer.readVarInt()));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.protocolVersion);
        packetdataserializer.writeUtf(this.hostName);
        packetdataserializer.writeShort(this.port);
        packetdataserializer.writeVarInt(this.intention.id());
    }

    @Override
    public PacketType<PacketHandshakingInSetProtocol> type() {
        return HandshakePacketTypes.CLIENT_INTENTION;
    }

    public void handle(PacketHandshakingInListener packethandshakinginlistener) {
        packethandshakinginlistener.handleIntention(this);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
