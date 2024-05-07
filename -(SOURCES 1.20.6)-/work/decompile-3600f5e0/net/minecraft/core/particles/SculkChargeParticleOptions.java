package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SculkChargeParticleOptions(float roll) implements ParticleParam {

    public static final MapCodec<SculkChargeParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.FLOAT.fieldOf("roll").forGetter((sculkchargeparticleoptions) -> {
            return sculkchargeparticleoptions.roll;
        })).apply(instance, SculkChargeParticleOptions::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, SculkChargeParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, (sculkchargeparticleoptions) -> {
        return sculkchargeparticleoptions.roll;
    }, SculkChargeParticleOptions::new);

    @Override
    public Particle<SculkChargeParticleOptions> getType() {
        return Particles.SCULK_CHARGE;
    }
}
