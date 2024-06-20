package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3D;
import org.joml.Vector3f;

public class ParticleParamRedstone extends DustParticleOptionsBase {

    public static final Vector3f REDSTONE_PARTICLE_COLOR = Vec3D.fromRGB24(16711680).toVector3f();
    public static final ParticleParamRedstone REDSTONE = new ParticleParamRedstone(ParticleParamRedstone.REDSTONE_PARTICLE_COLOR, 1.0F);
    public static final MapCodec<ParticleParamRedstone> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((particleparamredstone) -> {
            return particleparamredstone.color;
        }), ParticleParamRedstone.SCALE.fieldOf("scale").forGetter(DustParticleOptionsBase::getScale)).apply(instance, ParticleParamRedstone::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleParamRedstone> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VECTOR3F, (particleparamredstone) -> {
        return particleparamredstone.color;
    }, ByteBufCodecs.FLOAT, DustParticleOptionsBase::getScale, ParticleParamRedstone::new);
    private final Vector3f color;

    public ParticleParamRedstone(Vector3f vector3f, float f) {
        super(f);
        this.color = vector3f;
    }

    @Override
    public Particle<ParticleParamRedstone> getType() {
        return Particles.DUST;
    }

    public Vector3f getColor() {
        return this.color;
    }
}
