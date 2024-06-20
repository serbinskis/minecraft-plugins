package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;

public class PacketPlayOutSpawnEntity implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutSpawnEntity> STREAM_CODEC = Packet.codec(PacketPlayOutSpawnEntity::write, PacketPlayOutSpawnEntity::new);
    private static final double MAGICAL_QUANTIZATION = 8000.0D;
    private static final double LIMIT = 3.9D;
    private final int id;
    private final UUID uuid;
    private final EntityTypes<?> type;
    private final double x;
    private final double y;
    private final double z;
    private final int xa;
    private final int ya;
    private final int za;
    private final byte xRot;
    private final byte yRot;
    private final byte yHeadRot;
    private final int data;

    public PacketPlayOutSpawnEntity(Entity entity, EntityTrackerEntry entitytrackerentry) {
        this(entity, entitytrackerentry, 0);
    }

    public PacketPlayOutSpawnEntity(Entity entity, EntityTrackerEntry entitytrackerentry, int i) {
        this(entity.getId(), entity.getUUID(), entitytrackerentry.getPositionBase().x(), entitytrackerentry.getPositionBase().y(), entitytrackerentry.getPositionBase().z(), entitytrackerentry.getLastSentXRot(), entitytrackerentry.getLastSentYRot(), entity.getType(), i, entitytrackerentry.getLastSentMovement(), (double) entitytrackerentry.getLastSentYHeadRot());
    }

    public PacketPlayOutSpawnEntity(Entity entity, int i, BlockPosition blockposition) {
        this(entity.getId(), entity.getUUID(), (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), entity.getXRot(), entity.getYRot(), entity.getType(), i, entity.getDeltaMovement(), (double) entity.getYHeadRot());
    }

    public PacketPlayOutSpawnEntity(int i, UUID uuid, double d0, double d1, double d2, float f, float f1, EntityTypes<?> entitytypes, int j, Vec3D vec3d, double d3) {
        this.id = i;
        this.uuid = uuid;
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.xRot = (byte) MathHelper.floor(f * 256.0F / 360.0F);
        this.yRot = (byte) MathHelper.floor(f1 * 256.0F / 360.0F);
        this.yHeadRot = (byte) MathHelper.floor(d3 * 256.0D / 360.0D);
        this.type = entitytypes;
        this.data = j;
        this.xa = (int) (MathHelper.clamp(vec3d.x, -3.9D, 3.9D) * 8000.0D);
        this.ya = (int) (MathHelper.clamp(vec3d.y, -3.9D, 3.9D) * 8000.0D);
        this.za = (int) (MathHelper.clamp(vec3d.z, -3.9D, 3.9D) * 8000.0D);
    }

    private PacketPlayOutSpawnEntity(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.id = registryfriendlybytebuf.readVarInt();
        this.uuid = registryfriendlybytebuf.readUUID();
        this.type = (EntityTypes) ByteBufCodecs.registry(Registries.ENTITY_TYPE).decode(registryfriendlybytebuf);
        this.x = registryfriendlybytebuf.readDouble();
        this.y = registryfriendlybytebuf.readDouble();
        this.z = registryfriendlybytebuf.readDouble();
        this.xRot = registryfriendlybytebuf.readByte();
        this.yRot = registryfriendlybytebuf.readByte();
        this.yHeadRot = registryfriendlybytebuf.readByte();
        this.data = registryfriendlybytebuf.readVarInt();
        this.xa = registryfriendlybytebuf.readShort();
        this.ya = registryfriendlybytebuf.readShort();
        this.za = registryfriendlybytebuf.readShort();
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeVarInt(this.id);
        registryfriendlybytebuf.writeUUID(this.uuid);
        ByteBufCodecs.registry(Registries.ENTITY_TYPE).encode(registryfriendlybytebuf, this.type);
        registryfriendlybytebuf.writeDouble(this.x);
        registryfriendlybytebuf.writeDouble(this.y);
        registryfriendlybytebuf.writeDouble(this.z);
        registryfriendlybytebuf.writeByte(this.xRot);
        registryfriendlybytebuf.writeByte(this.yRot);
        registryfriendlybytebuf.writeByte(this.yHeadRot);
        registryfriendlybytebuf.writeVarInt(this.data);
        registryfriendlybytebuf.writeShort(this.xa);
        registryfriendlybytebuf.writeShort(this.ya);
        registryfriendlybytebuf.writeShort(this.za);
    }

    @Override
    public PacketType<PacketPlayOutSpawnEntity> type() {
        return GamePacketTypes.CLIENTBOUND_ADD_ENTITY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleAddEntity(this);
    }

    public int getId() {
        return this.id;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public EntityTypes<?> getType() {
        return this.type;
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

    public double getXa() {
        return (double) this.xa / 8000.0D;
    }

    public double getYa() {
        return (double) this.ya / 8000.0D;
    }

    public double getZa() {
        return (double) this.za / 8000.0D;
    }

    public float getXRot() {
        return (float) (this.xRot * 360) / 256.0F;
    }

    public float getYRot() {
        return (float) (this.yRot * 360) / 256.0F;
    }

    public float getYHeadRot() {
        return (float) (this.yHeadRot * 360) / 256.0F;
    }

    public int getData() {
        return this.data;
    }
}
