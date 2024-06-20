package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;

public class UniformInt extends IntProvider {

    public static final MapCodec<UniformInt> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.INT.fieldOf("min_inclusive").forGetter((uniformint) -> {
            return uniformint.minInclusive;
        }), Codec.INT.fieldOf("max_inclusive").forGetter((uniformint) -> {
            return uniformint.maxInclusive;
        })).apply(instance, UniformInt::new);
    }).validate((uniformint) -> {
        return uniformint.maxInclusive < uniformint.minInclusive ? DataResult.error(() -> {
            return "Max must be at least min, min_inclusive: " + uniformint.minInclusive + ", max_inclusive: " + uniformint.maxInclusive;
        }) : DataResult.success(uniformint);
    });
    private final int minInclusive;
    private final int maxInclusive;

    private UniformInt(int i, int j) {
        this.minInclusive = i;
        this.maxInclusive = j;
    }

    public static UniformInt of(int i, int j) {
        return new UniformInt(i, j);
    }

    @Override
    public int sample(RandomSource randomsource) {
        return MathHelper.randomBetweenInclusive(randomsource, this.minInclusive, this.maxInclusive);
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
        return IntProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}
