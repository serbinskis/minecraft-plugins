package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

public class PacketPlayOutEntityTeleport implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutEntityTeleport> STREAM_CODEC = Packet.codec(PacketPlayOutEntityTeleport::write, PacketPlayOutEntityTeleport::new);
    private final int id;
    private final double x;
    private final double y;
    private final double z;
    private final byte yRot;
    private final byte xRot;
    private final boolean onGround;

    public PacketPlayOutEntityTeleport(Entity entity) {
        this.id = entity.getId();
        Vec3D vec3d = entity.trackingPosition();

        this.x = vec3d.x;
        this.y = vec3d.y;
        this.z = vec3d.z;
        this.yRot = (byte) ((int) (entity.getYRot() * 256.0F / 360.0F));
        this.xRot = (byte) ((int) (entity.getXRot() * 256.0F / 360.0F));
        this.onGround = entity.onGround();
    }

    private PacketPlayOutEntityTeleport(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readVarInt();
        this.x = packetdataserializer.readDouble();
        this.y = packetdataserializer.readDouble();
        this.z = packetdataserializer.readDouble();
        this.yRot = packetdataserializer.readByte();
        this.xRot = packetdataserializer.readByte();
        this.onGround = packetdataserializer.readBoolean();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.id);
        packetdataserializer.writeDouble(this.x);
        packetdataserializer.writeDouble(this.y);
        packetdataserializer.writeDouble(this.z);
        packetdataserializer.writeByte(this.yRot);
        packetdataserializer.writeByte(this.xRot);
        packetdataserializer.writeBoolean(this.onGround);
    }

    @Override
    public PacketType<PacketPlayOutEntityTeleport> type() {
        return GamePacketTypes.CLIENTBOUND_TELEPORT_ENTITY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTeleportEntity(this);
    }

    public int getId() {
        return this.id;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public byte getyRot() {
        return this.yRot;
    }

    public byte getxRot() {
        return this.xRot;
    }

    public boolean isOnGround() {
        return this.onGround;
    }
}
