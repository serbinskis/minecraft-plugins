package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;

public class ClampedNormalInt extends IntProvider {

    public static final MapCodec<ClampedNormalInt> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.FLOAT.fieldOf("mean").forGetter((clampednormalint) -> {
            return clampednormalint.mean;
        }), Codec.FLOAT.fieldOf("deviation").forGetter((clampednormalint) -> {
            return clampednormalint.deviation;
        }), Codec.INT.fieldOf("min_inclusive").forGetter((clampednormalint) -> {
            return clampednormalint.minInclusive;
        }), Codec.INT.fieldOf("max_inclusive").forGetter((clampednormalint) -> {
            return clampednormalint.maxInclusive;
        })).apply(instance, ClampedNormalInt::new);
    }).validate((clampednormalint) -> {
        return clampednormalint.maxInclusive < clampednormalint.minInclusive ? DataResult.error(() -> {
            return "Max must be larger than min: [" + clampednormalint.minInclusive + ", " + clampednormalint.maxInclusive + "]";
        }) : DataResult.success(clampednormalint);
    });
    private final float mean;
    private final float deviation;
    private final int minInclusive;
    private final int maxInclusive;

    public static ClampedNormalInt of(float f, float f1, int i, int j) {
        return new ClampedNormalInt(f, f1, i, j);
    }

    private ClampedNormalInt(float f, float f1, int i, int j) {
        this.mean = f;
        this.deviation = f1;
        this.minInclusive = i;
        this.maxInclusive = j;
    }

    @Override
    public int sample(RandomSource randomsource) {
        return sample(randomsource, this.mean, this.deviation, (float) this.minInclusive, (float) this.maxInclusive);
    }

    public static int sample(RandomSource randomsource, float f, float f1, float f2, float f3) {
        return (int) MathHelper.clamp(MathHelper.normal(randomsource, f, f1), f2, f3);
    }

    @Override
    public int getMinValue() {
        return this.minInclusive;
    }

    @Override
    public int getMaxValue() {
        return this.maxInclusive;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}
