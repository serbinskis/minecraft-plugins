package net.minecraft.world.item.enchantment;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.MathHelper;

public interface LevelBasedValue {

    Codec<LevelBasedValue> DISPATCH_CODEC = BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.byNameCodec().dispatch(LevelBasedValue::codec, (mapcodec) -> {
        return mapcodec;
    });
    Codec<LevelBasedValue> CODEC = Codec.either(LevelBasedValue.b.CODEC, LevelBasedValue.DISPATCH_CODEC).xmap((either) -> {
        return (LevelBasedValue) either.map((levelbasedvalue_b) -> {
            return levelbasedvalue_b;
        }, (levelbasedvalue) -> {
            return levelbasedvalue;
        });
    }, (levelbasedvalue) -> {
        Either either;

        if (levelbasedvalue instanceof LevelBasedValue.b levelbasedvalue_b) {
            either = Either.left(levelbasedvalue_b);
        } else {
            either = Either.right(levelbasedvalue);
        }

        return either;
    });

    static MapCodec<? extends LevelBasedValue> bootstrap(IRegistry<MapCodec<? extends LevelBasedValue>> iregistry) {
        IRegistry.register(iregistry, "clamped", LevelBasedValue.a.CODEC);
        IRegistry.register(iregistry, "fraction", LevelBasedValue.c.CODEC);
        IRegistry.register(iregistry, "levels_squared", LevelBasedValue.d.CODEC);
        IRegistry.register(iregistry, "linear", LevelBasedValue.e.CODEC);
        return (MapCodec) IRegistry.register(iregistry, "lookup", LevelBasedValue.f.CODEC);
    }

    static LevelBasedValue.b constant(float f) {
        return new LevelBasedValue.b(f);
    }

    static LevelBasedValue.e perLevel(float f, float f1) {
        return new LevelBasedValue.e(f, f1);
    }

    static LevelBasedValue.e perLevel(float f) {
        return perLevel(f, f);
    }

    static LevelBasedValue.f lookup(List<Float> list, LevelBasedValue levelbasedvalue) {
        return new LevelBasedValue.f(list, levelbasedvalue);
    }

    float calculate(int i);

    MapCodec<? extends LevelBasedValue> codec();

    public static record a(LevelBasedValue value, float min, float max) implements LevelBasedValue {

        public static final MapCodec<LevelBasedValue.a> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(LevelBasedValue.CODEC.fieldOf("value").forGetter(LevelBasedValue.a::value), Codec.FLOAT.fieldOf("min").forGetter(LevelBasedValue.a::min), Codec.FLOAT.fieldOf("max").forGetter(LevelBasedValue.a::max)).apply(instance, LevelBasedValue.a::new);
        }).validate((levelbasedvalue_a) -> {
            return levelbasedvalue_a.max <= levelbasedvalue_a.min ? DataResult.error(() -> {
                return "Max must be larger than min, min: " + levelbasedvalue_a.min + ", max: " + levelbasedvalue_a.max;
            }) : DataResult.success(levelbasedvalue_a);
        });

        @Override
        public float calculate(int i) {
            return MathHelper.clamp(this.value.calculate(i), this.min, this.max);
        }

        @Override
        public MapCodec<LevelBasedValue.a> codec() {
            return LevelBasedValue.a.CODEC;
        }
    }

    public static record c(LevelBasedValue numerator, LevelBasedValue denominator) implements LevelBasedValue {

        public static final MapCodec<LevelBasedValue.c> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(LevelBasedValue.CODEC.fieldOf("numerator").forGetter(LevelBasedValue.c::numerator), LevelBasedValue.CODEC.fieldOf("denominator").forGetter(LevelBasedValue.c::denominator)).apply(instance, LevelBasedValue.c::new);
        });

        @Override
        public float calculate(int i) {
            float f = this.denominator.calculate(i);

            return f == 0.0F ? 0.0F : this.numerator.calculate(i) / f;
        }

        @Override
        public MapCodec<LevelBasedValue.c> codec() {
            return LevelBasedValue.c.CODEC;
        }
    }

    public static record d(float added) implements LevelBasedValue {

        public static final MapCodec<LevelBasedValue.d> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.FLOAT.fieldOf("added").forGetter(LevelBasedValue.d::added)).apply(instance, LevelBasedValue.d::new);
        });

        @Override
        public float calculate(int i) {
            return (float) MathHelper.square(i) + this.added;
        }

        @Override
        public MapCodec<LevelBasedValue.d> codec() {
            return LevelBasedValue.d.CODEC;
        }
    }

    public static record e(float base, float perLevelAboveFirst) implements LevelBasedValue {

        public static final MapCodec<LevelBasedValue.e> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.FLOAT.fieldOf("base").forGetter(LevelBasedValue.e::base), Codec.FLOAT.fieldOf("per_level_above_first").forGetter(LevelBasedValue.e::perLevelAboveFirst)).apply(instance, LevelBasedValue.e::new);
        });

        @Override
        public float calculate(int i) {
            return this.base + this.perLevelAboveFirst * (float) (i - 1);
        }

        @Override
        public MapCodec<LevelBasedValue.e> codec() {
            return LevelBasedValue.e.CODEC;
        }
    }

    public static record f(List<Float> values, LevelBasedValue fallback) implements LevelBasedValue {

        public static final MapCodec<LevelBasedValue.f> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.FLOAT.listOf().fieldOf("values").forGetter(LevelBasedValue.f::values), LevelBasedValue.CODEC.fieldOf("fallback").forGetter(LevelBasedValue.f::fallback)).apply(instance, LevelBasedValue.f::new);
        });

        @Override
        public float calculate(int i) {
            return i <= this.values.size() ? (Float) this.values.get(i - 1) : this.fallback.calculate(i);
        }

        @Override
        public MapCodec<LevelBasedValue.f> codec() {
            return LevelBasedValue.f.CODEC;
        }
    }

    public static record b(float value) implements LevelBasedValue {

        public static final Codec<LevelBasedValue.b> CODEC = Codec.FLOAT.xmap(LevelBasedValue.b::new, LevelBasedValue.b::value);
        public static final MapCodec<LevelBasedValue.b> TYPED_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.FLOAT.fieldOf("value").forGetter(LevelBasedValue.b::value)).apply(instance, LevelBasedValue.b::new);
        });

        @Override
        public float calculate(int i) {
            return this.value;
        }

        @Override
        public MapCodec<LevelBasedValue.b> codec() {
            return LevelBasedValue.b.TYPED_CODEC;
        }
    }
}
