package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class IntRange {

    private static final Codec<IntRange> RECORD_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(NumberProviders.CODEC.optionalFieldOf("min").forGetter((intrange) -> {
            return Optional.ofNullable(intrange.min);
        }), NumberProviders.CODEC.optionalFieldOf("max").forGetter((intrange) -> {
            return Optional.ofNullable(intrange.max);
        })).apply(instance, IntRange::new);
    });
    public static final Codec<IntRange> CODEC = Codec.either(Codec.INT, IntRange.RECORD_CODEC).xmap((either) -> {
        return (IntRange) either.map(IntRange::exact, Function.identity());
    }, (intrange) -> {
        OptionalInt optionalint = intrange.unpackExact();

        return optionalint.isPresent() ? Either.left(optionalint.getAsInt()) : Either.right(intrange);
    });
    @Nullable
    private final NumberProvider min;
    @Nullable
    private final NumberProvider max;
    private final IntRange.b limiter;
    private final IntRange.a predicate;

    public Set<LootContextParameter<?>> getReferencedContextParams() {
        Builder<LootContextParameter<?>> builder = ImmutableSet.builder();

        if (this.min != null) {
            builder.addAll(this.min.getReferencedContextParams());
        }

        if (this.max != null) {
            builder.addAll(this.max.getReferencedContextParams());
        }

        return builder.build();
    }

    private IntRange(Optional<NumberProvider> optional, Optional<NumberProvider> optional1) {
        this((NumberProvider) optional.orElse((Object) null), (NumberProvider) optional1.orElse((Object) null));
    }

    private IntRange(@Nullable NumberProvider numberprovider, @Nullable NumberProvider numberprovider1) {
        this.min = numberprovider;
        this.max = numberprovider1;
        if (numberprovider == null) {
            if (numberprovider1 == null) {
                this.limiter = (loottableinfo, i) -> {
                    return i;
                };
                this.predicate = (loottableinfo, i) -> {
                    return true;
                };
            } else {
                this.limiter = (loottableinfo, i) -> {
                    return Math.min(numberprovider1.getInt(loottableinfo), i);
                };
                this.predicate = (loottableinfo, i) -> {
                    return i <= numberprovider1.getInt(loottableinfo);
                };
            }
        } else if (numberprovider1 == null) {
            this.limiter = (loottableinfo, i) -> {
                return Math.max(numberprovider.getInt(loottableinfo), i);
            };
            this.predicate = (loottableinfo, i) -> {
                return i >= numberprovider.getInt(loottableinfo);
            };
        } else {
            this.limiter = (loottableinfo, i) -> {
                return MathHelper.clamp(i, numberprovider.getInt(loottableinfo), numberprovider1.getInt(loottableinfo));
            };
            this.predicate = (loottableinfo, i) -> {
                return i >= numberprovider.getInt(loottableinfo) && i <= numberprovider1.getInt(loottableinfo);
            };
        }

    }

    public static IntRange exact(int i) {
        ConstantValue constantvalue = ConstantValue.exactly((float) i);

        return new IntRange(Optional.of(constantvalue), Optional.of(constantvalue));
    }

    public static IntRange range(int i, int j) {
        return new IntRange(Optional.of(ConstantValue.exactly((float) i)), Optional.of(ConstantValue.exactly((float) j)));
    }

    public static IntRange lowerBound(int i) {
        return new IntRange(Optional.of(ConstantValue.exactly((float) i)), Optional.empty());
    }

    public static IntRange upperBound(int i) {
        return new IntRange(Optional.empty(), Optional.of(ConstantValue.exactly((float) i)));
    }

    public int clamp(LootTableInfo loottableinfo, int i) {
        return this.limiter.apply(loottableinfo, i);
    }

    public boolean test(LootTableInfo loottableinfo, int i) {
        return this.predicate.test(loottableinfo, i);
    }

    private OptionalInt unpackExact() {
        if (Objects.equals(this.min, this.max)) {
            NumberProvider numberprovider = this.min;

            if (numberprovider instanceof ConstantValue) {
                ConstantValue constantvalue = (ConstantValue) numberprovider;

                if (Math.floor((double) constantvalue.value()) == (double) constantvalue.value()) {
                    return OptionalInt.of((int) constantvalue.value());
                }
            }
        }

        return OptionalInt.empty();
    }

    @FunctionalInterface
    private interface b {

        int apply(LootTableInfo loottableinfo, int i);
    }

    @FunctionalInterface
    private interface a {

        boolean test(LootTableInfo loottableinfo, int i);
    }
}
