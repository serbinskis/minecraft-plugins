package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ParticleType extends Particle<ParticleType> implements ParticleParam {

    private final MapCodec<ParticleType> codec = MapCodec.unit(this::getType);
    private final StreamCodec<RegistryFriendlyByteBuf, ParticleType> streamCodec = StreamCodec.unit(this);

    protected ParticleType(boolean flag) {
        super(flag);
    }

    @Override
    public ParticleType getType() {
        return this;
    }

    @Override
    public MapCodec<ParticleType> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ParticleType> streamCodec() {
        return this.streamCodec;
    }
}
