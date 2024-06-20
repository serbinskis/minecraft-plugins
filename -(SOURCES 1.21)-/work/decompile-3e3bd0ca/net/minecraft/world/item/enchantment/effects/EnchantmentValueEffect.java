package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;

public interface EnchantmentValueEffect {

    Codec<EnchantmentValueEffect> CODEC = BuiltInRegistries.ENCHANTMENT_VALUE_EFFECT_TYPE.byNameCodec().dispatch(EnchantmentValueEffect::codec, Function.identity());

    static MapCodec<? extends EnchantmentValueEffect> bootstrap(IRegistry<MapCodec<? extends EnchantmentValueEffect>> iregistry) {
        IRegistry.register(iregistry, "add", AddValue.CODEC);
        IRegistry.register(iregistry, "all_of", AllOf.c.CODEC);
        IRegistry.register(iregistry, "multiply", MultiplyValue.CODEC);
        IRegistry.register(iregistry, "remove_binomial", RemoveBinomial.CODEC);
        return (MapCodec) IRegistry.register(iregistry, "set", SetValue.CODEC);
    }

    float process(int i, RandomSource randomsource, float f);

    MapCodec<? extends EnchantmentValueEffect> codec();
}
