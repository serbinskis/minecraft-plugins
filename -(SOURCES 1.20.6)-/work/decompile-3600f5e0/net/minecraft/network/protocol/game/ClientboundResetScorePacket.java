package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundResetScorePacket(String owner, @Nullable String objectiveName) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundResetScorePacket> STREAM_CODEC = Packet.codec(ClientboundResetScorePacket::write, ClientboundResetScorePacket::new);

    private ClientboundResetScorePacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(), (String) packetdataserializer.readNullable(PacketDataSerializer::readUtf));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.owner);
        packetdataserializer.writeNullable(this.objectiveName, PacketDataSerializer::writeUtf);
    }

    @Override
    public PacketType<ClientboundResetScorePacket> type() {
        return GamePacketTypes.CLIENTBOUND_RESET_SCORE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleResetScore(this);
    }
}
