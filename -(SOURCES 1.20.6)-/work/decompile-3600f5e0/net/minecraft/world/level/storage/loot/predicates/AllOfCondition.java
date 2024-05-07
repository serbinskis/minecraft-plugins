package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.SystemUtils;

public class AllOfCondition extends CompositeLootItemCondition {

    public static final MapCodec<AllOfCondition> CODEC = createCodec(AllOfCondition::new);
    public static final Codec<AllOfCondition> INLINE_CODEC = createInlineCodec(AllOfCondition::new);

    AllOfCondition(List<LootItemCondition> list) {
        super(list, SystemUtils.allOf(list));
    }

    public static AllOfCondition allOf(List<LootItemCondition> list) {
        return new AllOfCondition(List.copyOf(list));
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ALL_OF;
    }

    public static AllOfCondition.a allOf(LootItemCondition.a... alootitemcondition_a) {
        return new AllOfCondition.a(alootitemcondition_a);
    }

    public static class a extends CompositeLootItemCondition.a {

        public a(LootItemCondition.a... alootitemcondition_a) {
            super(alootitemcondition_a);
        }

        @Override
        public AllOfCondition.a and(LootItemCondition.a lootitemcondition_a) {
            this.addTerm(lootitemcondition_a);
            return this;
        }

        @Override
        protected LootItemCondition create(List<LootItemCondition> list) {
            return new AllOfCondition(list);
        }
    }
}
