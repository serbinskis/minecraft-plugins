package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.World;

public class PacketPlayOutEntityHeadRotation implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutEntityHeadRotation> STREAM_CODEC = Packet.codec(PacketPlayOutEntityHeadRotation::write, PacketPlayOutEntityHeadRotation::new);
    private final int entityId;
    private final byte yHeadRot;

    public PacketPlayOutEntityHeadRotation(Entity entity, byte b0) {
        this.entityId = entity.getId();
        this.yHeadRot = b0;
    }

    private PacketPlayOutEntityHeadRotation(PacketDataSerializer packetdataserializer) {
        this.entityId = packetdataserializer.readVarInt();
        this.yHeadRot = packetdataserializer.readByte();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.entityId);
        packetdataserializer.writeByte(this.yHeadRot);
    }

    @Override
    public PacketType<PacketPlayOutEntityHeadRotation> type() {
        return GamePacketTypes.CLIENTBOUND_ROTATE_HEAD;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleRotateMob(this);
    }

    public Entity getEntity(World world) {
        return world.getEntity(this.entityId);
    }

    public byte getYHeadRot() {
        return this.yHeadRot;
    }
}
