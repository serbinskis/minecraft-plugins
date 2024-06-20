package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;

public class UniformFloat extends FloatProvider {

    public static final MapCodec<UniformFloat> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.FLOAT.fieldOf("min_inclusive").forGetter((uniformfloat) -> {
            return uniformfloat.minInclusive;
        }), Codec.FLOAT.fieldOf("max_exclusive").forGetter((uniformfloat) -> {
            return uniformfloat.maxExclusive;
        })).apply(instance, UniformFloat::new);
    }).validate((uniformfloat) -> {
        return uniformfloat.maxExclusive <= uniformfloat.minInclusive ? DataResult.error(() -> {
            return "Max must be larger than min, min_inclusive: " + uniformfloat.minInclusive + ", max_exclusive: " + uniformfloat.maxExclusive;
        }) : DataResult.success(uniformfloat);
    });
    private final float minInclusive;
    private final float maxExclusive;

    private UniformFloat(float f, float f1) {
        this.minInclusive = f;
        this.maxExclusive = f1;
    }

    public static UniformFloat of(float f, float f1) {
        if (f1 <= f) {
            throw new IllegalArgumentException("Max must exceed min");
        } else {
            return new UniformFloat(f, f1);
        }
    }

    @Override
    public float sample(RandomSource randomsource) {
        return MathHelper.randomBetween(randomsource, this.minInclusive, this.maxExclusive);
    }

    @Override
    public float getMinValue() {
        return this.minInclusive;
    }

    @Override
    public float getMaxValue() {
        return this.maxExclusive;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxExclusive + "]";
    }
}
