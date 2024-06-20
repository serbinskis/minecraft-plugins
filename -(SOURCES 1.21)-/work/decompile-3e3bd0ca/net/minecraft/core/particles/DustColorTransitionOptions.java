package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3D;
import org.joml.Vector3f;

public class DustColorTransitionOptions extends DustParticleOptionsBase {

    public static final Vector3f SCULK_PARTICLE_COLOR = Vec3D.fromRGB24(3790560).toVector3f();
    public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(DustColorTransitionOptions.SCULK_PARTICLE_COLOR, ParticleParamRedstone.REDSTONE_PARTICLE_COLOR, 1.0F);
    public static final MapCodec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.VECTOR3F.fieldOf("from_color").forGetter((dustcolortransitionoptions) -> {
            return dustcolortransitionoptions.fromColor;
        }), ExtraCodecs.VECTOR3F.fieldOf("to_color").forGetter((dustcolortransitionoptions) -> {
            return dustcolortransitionoptions.toColor;
        }), DustColorTransitionOptions.SCALE.fieldOf("scale").forGetter(DustParticleOptionsBase::getScale)).apply(instance, DustColorTransitionOptions::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DustColorTransitionOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VECTOR3F, (dustcolortransitionoptions) -> {
        return dustcolortransitionoptions.fromColor;
    }, ByteBufCodecs.VECTOR3F, (dustcolortransitionoptions) -> {
        return dustcolortransitionoptions.toColor;
    }, ByteBufCodecs.FLOAT, DustParticleOptionsBase::getScale, DustColorTransitionOptions::new);
    private final Vector3f fromColor;
    private final Vector3f toColor;

    public DustColorTransitionOptions(Vector3f vector3f, Vector3f vector3f1, float f) {
        super(f);
        this.fromColor = vector3f;
        this.toColor = vector3f1;
    }

    public Vector3f getFromColor() {
        return this.fromColor;
    }

    public Vector3f getToColor() {
        return this.toColor;
    }

    @Override
    public Particle<DustColorTransitionOptions> getType() {
        return Particles.DUST_COLOR_TRANSITION;
    }
}
