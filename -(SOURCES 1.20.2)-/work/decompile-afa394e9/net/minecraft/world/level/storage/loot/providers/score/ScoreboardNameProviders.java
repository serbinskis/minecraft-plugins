package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;

public class ScoreboardNameProviders {

    private static final Codec<ScoreboardNameProvider> TYPED_CODEC = BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE.byNameCodec().dispatch(ScoreboardNameProvider::getType, LootScoreProviderType::codec);
    public static final Codec<ScoreboardNameProvider> CODEC = ExtraCodecs.lazyInitializedCodec(() -> {
        return Codec.either(ContextScoreboardNameProvider.INLINE_CODEC, ScoreboardNameProviders.TYPED_CODEC).xmap((either) -> {
            return (ScoreboardNameProvider) either.map(Function.identity(), Function.identity());
        }, (scoreboardnameprovider) -> {
            Either either;

            if (scoreboardnameprovider instanceof ContextScoreboardNameProvider) {
                ContextScoreboardNameProvider contextscoreboardnameprovider = (ContextScoreboardNameProvider) scoreboardnameprovider;

                either = Either.left(contextscoreboardnameprovider);
            } else {
                either = Either.right(scoreboardnameprovider);
            }

            return either;
        });
    });
    public static final LootScoreProviderType FIXED = register("fixed", FixedScoreboardNameProvider.CODEC);
    public static final LootScoreProviderType CONTEXT = register("context", ContextScoreboardNameProvider.CODEC);

    public ScoreboardNameProviders() {}

    private static LootScoreProviderType register(String s, Codec<? extends ScoreboardNameProvider> codec) {
        return (LootScoreProviderType) IRegistry.register(BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, new MinecraftKey(s), new LootScoreProviderType(codec));
    }
}
