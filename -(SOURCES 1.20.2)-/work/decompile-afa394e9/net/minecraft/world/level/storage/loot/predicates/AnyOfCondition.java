package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import java.util.List;

public class AnyOfCondition extends CompositeLootItemCondition {

    public static final Codec<AnyOfCondition> CODEC = createCodec(AnyOfCondition::new);

    AnyOfCondition(List<LootItemCondition> list) {
        super(list, LootItemConditions.orConditions(list));
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ANY_OF;
    }

    public static AnyOfCondition.a anyOf(LootItemCondition.a... alootitemcondition_a) {
        return new AnyOfCondition.a(alootitemcondition_a);
    }

    public static class a extends CompositeLootItemCondition.a {

        public a(LootItemCondition.a... alootitemcondition_a) {
            super(alootitemcondition_a);
        }

        @Override
        public AnyOfCondition.a or(LootItemCondition.a lootitemcondition_a) {
            this.addTerm(lootitemcondition_a);
            return this;
        }

        @Override
        protected LootItemCondition create(List<LootItemCondition> list) {
            return new AnyOfCondition(list);
        }
    }
}
