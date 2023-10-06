package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;

public class NumberProviders {

    private static final Codec<NumberProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE.byNameCodec().dispatch(NumberProvider::getType, LootNumberProviderType::codec);
    public static final Codec<NumberProvider> CODEC = ExtraCodecs.lazyInitializedCodec(() -> {
        Codec<NumberProvider> codec = ExtraCodecs.withAlternative(NumberProviders.TYPED_CODEC, UniformGenerator.CODEC);

        return Codec.either(ConstantValue.INLINE_CODEC, codec).xmap((either) -> {
            return (NumberProvider) either.map(Function.identity(), Function.identity());
        }, (numberprovider) -> {
            Either either;

            if (numberprovider instanceof ConstantValue) {
                ConstantValue constantvalue = (ConstantValue) numberprovider;

                either = Either.left(constantvalue);
            } else {
                either = Either.right(numberprovider);
            }

            return either;
        });
    });
    public static final LootNumberProviderType CONSTANT = register("constant", ConstantValue.CODEC);
    public static final LootNumberProviderType UNIFORM = register("uniform", UniformGenerator.CODEC);
    public static final LootNumberProviderType BINOMIAL = register("binomial", BinomialDistributionGenerator.CODEC);
    public static final LootNumberProviderType SCORE = register("score", ScoreboardValue.CODEC);

    public NumberProviders() {}

    private static LootNumberProviderType register(String s, Codec<? extends NumberProvider> codec) {
        return (LootNumberProviderType) IRegistry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, new MinecraftKey(s), new LootNumberProviderType(codec));
    }
}
