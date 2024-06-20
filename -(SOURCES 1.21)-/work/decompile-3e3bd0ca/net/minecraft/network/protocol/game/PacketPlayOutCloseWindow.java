package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutCloseWindow implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutCloseWindow> STREAM_CODEC = Packet.codec(PacketPlayOutCloseWindow::write, PacketPlayOutCloseWindow::new);
    private final int containerId;

    public PacketPlayOutCloseWindow(int i) {
        this.containerId = i;
    }

    private PacketPlayOutCloseWindow(PacketDataSerializer packetdataserializer) {
        this.containerId = packetdataserializer.readUnsignedByte();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeByte(this.containerId);
    }

    @Override
    public PacketType<PacketPlayOutCloseWindow> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_CLOSE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleContainerClose(this);
    }

    public int getContainerId() {
        return this.containerId;
    }
}
