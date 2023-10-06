package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;

public record FixedScoreboardNameProvider(String name) implements ScoreboardNameProvider {

    public static final Codec<FixedScoreboardNameProvider> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.STRING.fieldOf("name").forGetter(FixedScoreboardNameProvider::name)).apply(instance, FixedScoreboardNameProvider::new);
    });

    public static ScoreboardNameProvider forName(String s) {
        return new FixedScoreboardNameProvider(s);
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.FIXED;
    }

    @Nullable
    @Override
    public String getScoreboardName(LootTableInfo loottableinfo) {
        return this.name;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of();
    }
}
