package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayInBoatMove implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInBoatMove> STREAM_CODEC = Packet.codec(PacketPlayInBoatMove::write, PacketPlayInBoatMove::new);
    private final boolean left;
    private final boolean right;

    public PacketPlayInBoatMove(boolean flag, boolean flag1) {
        this.left = flag;
        this.right = flag1;
    }

    private PacketPlayInBoatMove(PacketDataSerializer packetdataserializer) {
        this.left = packetdataserializer.readBoolean();
        this.right = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBoolean(this.left);
        packetdataserializer.writeBoolean(this.right);
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handlePaddleBoat(this);
    }

    @Override
    public PacketType<PacketPlayInBoatMove> type() {
        return GamePacketTypes.SERVERBOUND_PADDLE_BOAT;
    }

    public boolean getLeft() {
        return this.left;
    }

    public boolean getRight() {
        return this.right;
    }
}
