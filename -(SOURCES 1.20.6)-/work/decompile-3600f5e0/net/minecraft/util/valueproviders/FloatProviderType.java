package net.minecraft.util.valueproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface FloatProviderType<P extends FloatProvider> {

    FloatProviderType<ConstantFloat> CONSTANT = register("constant", ConstantFloat.CODEC);
    FloatProviderType<UniformFloat> UNIFORM = register("uniform", UniformFloat.CODEC);
    FloatProviderType<ClampedNormalFloat> CLAMPED_NORMAL = register("clamped_normal", ClampedNormalFloat.CODEC);
    FloatProviderType<TrapezoidFloat> TRAPEZOID = register("trapezoid", TrapezoidFloat.CODEC);

    MapCodec<P> codec();

    static <P extends FloatProvider> FloatProviderType<P> register(String s, MapCodec<P> mapcodec) {
        return (FloatProviderType) IRegistry.register(BuiltInRegistries.FLOAT_PROVIDER_TYPE, s, () -> {
            return mapcodec;
        });
    }
}
