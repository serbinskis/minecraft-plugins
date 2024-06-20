package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutBlockBreakAnimation implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutBlockBreakAnimation> STREAM_CODEC = Packet.codec(PacketPlayOutBlockBreakAnimation::write, PacketPlayOutBlockBreakAnimation::new);
    private final int id;
    private final BlockPosition pos;
    private final int progress;

    public PacketPlayOutBlockBreakAnimation(int i, BlockPosition blockposition, int j) {
        this.id = i;
        this.pos = blockposition;
        this.progress = j;
    }

    private PacketPlayOutBlockBreakAnimation(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readVarInt();
        this.pos = packetdataserializer.readBlockPos();
        this.progress = packetdataserializer.readUnsignedByte();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.id);
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeByte(this.progress);
    }

    @Override
    public PacketType<PacketPlayOutBlockBreakAnimation> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_DESTRUCTION;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleBlockDestruction(this);
    }

    public int getId() {
        return this.id;
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public int getProgress() {
        return this.progress;
    }
}
