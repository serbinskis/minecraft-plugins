package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;

public class NumberProviders {

    private static final Codec<NumberProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE.byNameCodec().dispatch(NumberProvider::getType, LootNumberProviderType::codec);
    public static final Codec<NumberProvider> CODEC = Codec.lazyInitialized(() -> {
        Codec<NumberProvider> codec = Codec.withAlternative(NumberProviders.TYPED_CODEC, UniformGenerator.CODEC.codec());

        return Codec.either(ConstantValue.INLINE_CODEC, codec).xmap(Either::unwrap, (numberprovider) -> {
            Either either;

            if (numberprovider instanceof ConstantValue constantvalue) {
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
    public static final LootNumberProviderType STORAGE = register("storage", StorageValue.CODEC);
    public static final LootNumberProviderType ENCHANTMENT_LEVEL = register("enchantment_level", EnchantmentLevelProvider.CODEC);

    public NumberProviders() {}

    private static LootNumberProviderType register(String s, MapCodec<? extends NumberProvider> mapcodec) {
        return (LootNumberProviderType) IRegistry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, MinecraftKey.withDefaultNamespace(s), new LootNumberProviderType(mapcodec));
    }
}
