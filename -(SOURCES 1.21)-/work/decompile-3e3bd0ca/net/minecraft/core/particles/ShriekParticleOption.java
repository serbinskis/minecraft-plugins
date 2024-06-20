package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ShriekParticleOption implements ParticleParam {

    public static final MapCodec<ShriekParticleOption> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.INT.fieldOf("delay").forGetter((shriekparticleoption) -> {
            return shriekparticleoption.delay;
        })).apply(instance, ShriekParticleOption::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ShriekParticleOption> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, (shriekparticleoption) -> {
        return shriekparticleoption.delay;
    }, ShriekParticleOption::new);
    private final int delay;

    public ShriekParticleOption(int i) {
        this.delay = i;
    }

    @Override
    public Particle<ShriekParticleOption> getType() {
        return Particles.SHRIEK;
    }

    public int getDelay() {
        return this.delay;
    }
}
