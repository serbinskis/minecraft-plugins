package net.minecraft.world.level.storage.loot.providers.score;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.scores.ScoreHolder;

public interface ScoreboardNameProvider {

    @Nullable
    ScoreHolder getScoreHolder(LootTableInfo loottableinfo);

    LootScoreProviderType getType();

    Set<LootContextParameter<?>> getReferencedContextParams();
}
