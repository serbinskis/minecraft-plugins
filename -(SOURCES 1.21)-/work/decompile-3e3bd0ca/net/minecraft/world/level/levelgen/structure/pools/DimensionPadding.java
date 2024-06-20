package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public record DimensionPadding(int bottom, int top) {

    private static final Codec<DimensionPadding> RECORD_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.NON_NEGATIVE_INT.lenientOptionalFieldOf("bottom", 0).forGetter((dimensionpadding) -> {
            return dimensionpadding.bottom;
        }), ExtraCodecs.NON_NEGATIVE_INT.lenientOptionalFieldOf("top", 0).forGetter((dimensionpadding) -> {
            return dimensionpadding.top;
        })).apply(instance, DimensionPadding::new);
    });
    public static final Codec<DimensionPadding> CODEC = Codec.either(ExtraCodecs.NON_NEGATIVE_INT, DimensionPadding.RECORD_CODEC).xmap((either) -> {
        return (DimensionPadding) either.map(DimensionPadding::new, Function.identity());
    }, (dimensionpadding) -> {
        return dimensionpadding.hasEqualTopAndBottom() ? Either.left(dimensionpadding.bottom) : Either.right(dimensionpadding);
    });
    public static final DimensionPadding ZERO = new DimensionPadding(0);

    public DimensionPadding(int i) {
        this(i, i);
    }

    public boolean hasEqualTopAndBottom() {
        return this.top == this.bottom;
    }
}
