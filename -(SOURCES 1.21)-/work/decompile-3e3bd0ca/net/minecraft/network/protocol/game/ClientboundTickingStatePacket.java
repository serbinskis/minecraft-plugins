package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStatePacket(float tickRate, boolean isFrozen) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundTickingStatePacket> STREAM_CODEC = Packet.codec(ClientboundTickingStatePacket::write, ClientboundTickingStatePacket::new);

    private ClientboundTickingStatePacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readFloat(), packetdataserializer.readBoolean());
    }

    public static ClientboundTickingStatePacket from(TickRateManager tickratemanager) {
        return new ClientboundTickingStatePacket(tickratemanager.tickrate(), tickratemanager.isFrozen());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeFloat(this.tickRate);
        packetdataserializer.writeBoolean(this.isFrozen);
    }

    @Override
    public PacketType<ClientboundTickingStatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_TICKING_STATE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTickingState(this);
    }
}
