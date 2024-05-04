package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootItemUser;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public interface LootItemCondition extends LootItemUser, Predicate<LootTableInfo> {

    LootItemConditionType getType();

    @FunctionalInterface
    public interface a {

        LootItemCondition build();

        default LootItemCondition.a invert() {
            return LootItemConditionInverted.invert(this);
        }

        default AnyOfCondition.a or(LootItemCondition.a lootitemcondition_a) {
            return AnyOfCondition.anyOf(this, lootitemcondition_a);
        }

        default AllOfCondition.a and(LootItemCondition.a lootitemcondition_a) {
            return AllOfCondition.allOf(this, lootitemcondition_a);
        }
    }
}
