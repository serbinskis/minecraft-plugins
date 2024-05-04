package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3D;

public class PacketPlayOutExplosion implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutExplosion> STREAM_CODEC = Packet.codec(PacketPlayOutExplosion::write, PacketPlayOutExplosion::new);
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPosition> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;
    private final ParticleParam smallExplosionParticles;
    private final ParticleParam largeExplosionParticles;
    private final Explosion.Effect blockInteraction;
    private final Holder<SoundEffect> explosionSound;

    public PacketPlayOutExplosion(double d0, double d1, double d2, float f, List<BlockPosition> list, @Nullable Vec3D vec3d, Explosion.Effect explosion_effect, ParticleParam particleparam, ParticleParam particleparam1, Holder<SoundEffect> holder) {
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.power = f;
        this.toBlow = Lists.newArrayList(list);
        this.explosionSound = holder;
        if (vec3d != null) {
            this.knockbackX = (float) vec3d.x;
            this.knockbackY = (float) vec3d.y;
            this.knockbackZ = (float) vec3d.z;
        } else {
            this.knockbackX = 0.0F;
            this.knockbackY = 0.0F;
            this.knockbackZ = 0.0F;
        }

        this.blockInteraction = explosion_effect;
        this.smallExplosionParticles = particleparam;
        this.largeExplosionParticles = particleparam1;
    }

    private PacketPlayOutExplosion(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.x = registryfriendlybytebuf.readDouble();
        this.y = registryfriendlybytebuf.readDouble();
        this.z = registryfriendlybytebuf.readDouble();
        this.power = registryfriendlybytebuf.readFloat();
        int i = MathHelper.floor(this.x);
        int j = MathHelper.floor(this.y);
        int k = MathHelper.floor(this.z);

        this.toBlow = registryfriendlybytebuf.readList((packetdataserializer) -> {
            int l = packetdataserializer.readByte() + i;
            int i1 = packetdataserializer.readByte() + j;
            int j1 = packetdataserializer.readByte() + k;

            return new BlockPosition(l, i1, j1);
        });
        this.knockbackX = registryfriendlybytebuf.readFloat();
        this.knockbackY = registryfriendlybytebuf.readFloat();
        this.knockbackZ = registryfriendlybytebuf.readFloat();
        this.blockInteraction = (Explosion.Effect) registryfriendlybytebuf.readEnum(Explosion.Effect.class);
        this.smallExplosionParticles = (ParticleParam) Particles.STREAM_CODEC.decode(registryfriendlybytebuf);
        this.largeExplosionParticles = (ParticleParam) Particles.STREAM_CODEC.decode(registryfriendlybytebuf);
        this.explosionSound = (Holder) SoundEffect.STREAM_CODEC.decode(registryfriendlybytebuf);
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeDouble(this.x);
        registryfriendlybytebuf.writeDouble(this.y);
        registryfriendlybytebuf.writeDouble(this.z);
        registryfriendlybytebuf.writeFloat(this.power);
        int i = MathHelper.floor(this.x);
        int j = MathHelper.floor(this.y);
        int k = MathHelper.floor(this.z);

        registryfriendlybytebuf.writeCollection(this.toBlow, (packetdataserializer, blockposition) -> {
            int l = blockposition.getX() - i;
            int i1 = blockposition.getY() - j;
            int j1 = blockposition.getZ() - k;

            packetdataserializer.writeByte(l);
            packetdataserializer.writeByte(i1);
            packetdataserializer.writeByte(j1);
        });
        registryfriendlybytebuf.writeFloat(this.knockbackX);
        registryfriendlybytebuf.writeFloat(this.knockbackY);
        registryfriendlybytebuf.writeFloat(this.knockbackZ);
        registryfriendlybytebuf.writeEnum(this.blockInteraction);
        Particles.STREAM_CODEC.encode(registryfriendlybytebuf, this.smallExplosionParticles);
        Particles.STREAM_CODEC.encode(registryfriendlybytebuf, this.largeExplosionParticles);
        SoundEffect.STREAM_CODEC.encode(registryfriendlybytebuf, this.explosionSound);
    }

    @Override
    public PacketType<PacketPlayOutExplosion> type() {
        return GamePacketTypes.CLIENTBOUND_EXPLODE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleExplosion(this);
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
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

    public float getPower() {
        return this.power;
    }

    public List<BlockPosition> getToBlow() {
        return this.toBlow;
    }

    public Explosion.Effect getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleParam getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleParam getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public Holder<SoundEffect> getExplosionSound() {
        return this.explosionSound;
    }
}
