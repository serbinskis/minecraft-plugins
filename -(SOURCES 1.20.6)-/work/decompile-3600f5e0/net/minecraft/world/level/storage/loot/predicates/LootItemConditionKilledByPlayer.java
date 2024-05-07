package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class LootItemConditionKilledByPlayer implements LootItemCondition {

    private static final LootItemConditionKilledByPlayer INSTANCE = new LootItemConditionKilledByPlayer();
    public static final MapCodec<LootItemConditionKilledByPlayer> CODEC = MapCodec.unit(LootItemConditionKilledByPlayer.INSTANCE);

    private LootItemConditionKilledByPlayer() {}

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.KILLED_BY_PLAYER;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParameters.LAST_DAMAGE_PLAYER);
    }

    public boolean test(LootTableInfo loottableinfo) {
        return loottableinfo.hasParam(LootContextParameters.LAST_DAMAGE_PLAYER);
    }

    public static LootItemCondition.a killedByPlayer() {
        return () -> {
            return LootItemConditionKilledByPlayer.INSTANCE;
        };
    }
}
