package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderSizePacket implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundSetBorderSizePacket> STREAM_CODEC = Packet.codec(ClientboundSetBorderSizePacket::write, ClientboundSetBorderSizePacket::new);
    private final double size;

    public ClientboundSetBorderSizePacket(WorldBorder worldborder) {
        this.size = worldborder.getLerpTarget();
    }

    private ClientboundSetBorderSizePacket(PacketDataSerializer packetdataserializer) {
        this.size = packetdataserializer.readDouble();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeDouble(this.size);
    }

    @Override
    public PacketType<ClientboundSetBorderSizePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_BORDER_SIZE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetBorderSize(this);
    }

    public double getSize() {
        return this.size;
    }
}
