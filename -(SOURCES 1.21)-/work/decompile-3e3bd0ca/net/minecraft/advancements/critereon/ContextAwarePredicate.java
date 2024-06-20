package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.SystemUtils;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ContextAwarePredicate {

    public static final Codec<ContextAwarePredicate> CODEC = LootItemCondition.DIRECT_CODEC.listOf().xmap(ContextAwarePredicate::new, (contextawarepredicate) -> {
        return contextawarepredicate.conditions;
    });
    private final List<LootItemCondition> conditions;
    private final Predicate<LootTableInfo> compositePredicates;

    ContextAwarePredicate(List<LootItemCondition> list) {
        this.conditions = list;
        this.compositePredicates = SystemUtils.allOf(list);
    }

    public static ContextAwarePredicate create(LootItemCondition... alootitemcondition) {
        return new ContextAwarePredicate(List.of(alootitemcondition));
    }

    public boolean matches(LootTableInfo loottableinfo) {
        return this.compositePredicates.test(loottableinfo);
    }

    public void validate(LootCollector lootcollector) {
        for (int i = 0; i < this.conditions.size(); ++i) {
            LootItemCondition lootitemcondition = (LootItemCondition) this.conditions.get(i);

            lootitemcondition.validate(lootcollector.forChild("[" + i + "]"));
        }

    }
}
