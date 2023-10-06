package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;

public record ContextScoreboardNameProvider(LootTableInfo.EntityTarget target) implements ScoreboardNameProvider {

    public static final Codec<ContextScoreboardNameProvider> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(LootTableInfo.EntityTarget.CODEC.fieldOf("target").forGetter(ContextScoreboardNameProvider::target)).apply(instance, ContextScoreboardNameProvider::new);
    });
    public static final Codec<ContextScoreboardNameProvider> INLINE_CODEC = LootTableInfo.EntityTarget.CODEC.xmap(ContextScoreboardNameProvider::new, ContextScoreboardNameProvider::target);

    public static ScoreboardNameProvider forTarget(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return new ContextScoreboardNameProvider(loottableinfo_entitytarget);
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.CONTEXT;
    }

    @Nullable
    @Override
    public String getScoreboardName(LootTableInfo loottableinfo) {
        Entity entity = (Entity) loottableinfo.getParamOrNull(this.target.getParam());

        return entity != null ? entity.getScoreboardName() : null;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.target.getParam());
    }
}
