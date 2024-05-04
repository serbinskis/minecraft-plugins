package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.debugchart.RemoteDebugSampleType;

public record ClientboundDebugSamplePacket(long[] sample, RemoteDebugSampleType debugSampleType) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundDebugSamplePacket> STREAM_CODEC = Packet.codec(ClientboundDebugSamplePacket::write, ClientboundDebugSamplePacket::new);

    private ClientboundDebugSamplePacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readLongArray(), (RemoteDebugSampleType) packetdataserializer.readEnum(RemoteDebugSampleType.class));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeLongArray(this.sample);
        packetdataserializer.writeEnum(this.debugSampleType);
    }

    @Override
    public PacketType<ClientboundDebugSamplePacket> type() {
        return GamePacketTypes.CLIENTBOUND_DEBUG_SAMPLE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleDebugSample(this);
    }
}
