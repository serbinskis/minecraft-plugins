package net.minecraft.network.protocol.game;

import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundBundlePacket extends BundlePacket<PacketListenerPlayOut> {

    public ClientboundBundlePacket(Iterable<Packet<? super PacketListenerPlayOut>> iterable) {
        super(iterable);
    }

    @Override
    public PacketType<ClientboundBundlePacket> type() {
        return GamePacketTypes.CLIENTBOUND_BUNDLE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleBundlePacket(this);
    }
}
