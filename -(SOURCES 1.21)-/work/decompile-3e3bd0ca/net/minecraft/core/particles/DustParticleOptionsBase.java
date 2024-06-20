package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.MathHelper;

public abstract class DustParticleOptionsBase implements ParticleParam {

    public static final float MIN_SCALE = 0.01F;
    public static final float MAX_SCALE = 4.0F;
    protected static final Codec<Float> SCALE = Codec.FLOAT.validate((ofloat) -> {
        return ofloat >= 0.01F && ofloat <= 4.0F ? DataResult.success(ofloat) : DataResult.error(() -> {
            return "Value must be within range [0.01;4.0]: " + ofloat;
        });
    });
    private final float scale;

    public DustParticleOptionsBase(float f) {
        this.scale = MathHelper.clamp(f, 0.01F, 4.0F);
    }

    public float getScale() {
        return this.scale;
    }
}
