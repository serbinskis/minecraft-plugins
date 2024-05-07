package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;

public class ScoreboardNameProviders {

    private static final Codec<ScoreboardNameProvider> TYPED_CODEC = BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE.byNameCodec().dispatch(ScoreboardNameProvider::getType, LootScoreProviderType::codec);
    public static final Codec<ScoreboardNameProvider> CODEC = Codec.lazyInitialized(() -> {
        return Codec.either(ContextScoreboardNameProvider.INLINE_CODEC, ScoreboardNameProviders.TYPED_CODEC).xmap(Either::unwrap, (scoreboardnameprovider) -> {
            Either either;

            if (scoreboardnameprovider instanceof ContextScoreboardNameProvider contextscoreboardnameprovider) {
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

    private static LootScoreProviderType register(String s, MapCodec<? extends ScoreboardNameProvider> mapcodec) {
        return (LootScoreProviderType) IRegistry.register(BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, new MinecraftKey(s), new LootScoreProviderType(mapcodec));
    }
}
