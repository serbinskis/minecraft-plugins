package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInCloseWindow implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInCloseWindow> STREAM_CODEC = Packet.codec(PacketPlayInCloseWindow::write, PacketPlayInCloseWindow::new);
    private final int containerId;

    public PacketPlayInCloseWindow(int i) {
        this.containerId = i;
    }

    private PacketPlayInCloseWindow(PacketDataSerializer packetdataserializer) {
        this.containerId = packetdataserializer.readByte();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.containerId);
    }

    @Override
    public PacketType<PacketPlayInCloseWindow> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_CLOSE;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleContainerClose(this);
    }

    public int getContainerId() {
        return this.containerId;
    }
}
