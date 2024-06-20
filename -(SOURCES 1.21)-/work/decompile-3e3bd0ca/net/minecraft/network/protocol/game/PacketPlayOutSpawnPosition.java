package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutSpawnPosition implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutSpawnPosition> STREAM_CODEC = Packet.codec(PacketPlayOutSpawnPosition::write, PacketPlayOutSpawnPosition::new);
    public final BlockPosition pos;
    private final float angle;

    public PacketPlayOutSpawnPosition(BlockPosition blockposition, float f) {
        this.pos = blockposition;
        this.angle = f;
    }

    private PacketPlayOutSpawnPosition(PacketDataSerializer packetdataserializer) {
        this.pos = packetdataserializer.readBlockPos();
        this.angle = packetdataserializer.readFloat();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeFloat(this.angle);
    }

    @Override
    public PacketType<PacketPlayOutSpawnPosition> type() {
        return GamePacketTypes.CLIENTBOUND_SET_DEFAULT_SPAWN_POSITION;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetSpawn(this);
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    public float getAngle() {
        return this.angle;
    }
}
