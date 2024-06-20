package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class Particle<T extends ParticleParam> {

    private final boolean overrideLimiter;

    protected Particle(boolean flag) {
        this.overrideLimiter = flag;
    }

    public boolean getOverrideLimiter() {
        return this.overrideLimiter;
    }

    public abstract MapCodec<T> codec();

    public abstract StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();
}
