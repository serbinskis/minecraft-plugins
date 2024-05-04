package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDelayPacket implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundSetBorderWarningDelayPacket> STREAM_CODEC = Packet.codec(ClientboundSetBorderWarningDelayPacket::write, ClientboundSetBorderWarningDelayPacket::new);
    private final int warningDelay;

    public ClientboundSetBorderWarningDelayPacket(WorldBorder worldborder) {
        this.warningDelay = worldborder.getWarningTime();
    }

    private ClientboundSetBorderWarningDelayPacket(PacketDataSerializer packetdataserializer) {
        this.warningDelay = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.warningDelay);
    }

    @Override
    public PacketType<ClientboundSetBorderWarningDelayPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DELAY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetBorderWarningDelay(this);
    }

    public int getWarningDelay() {
        return this.warningDelay;
    }
}
