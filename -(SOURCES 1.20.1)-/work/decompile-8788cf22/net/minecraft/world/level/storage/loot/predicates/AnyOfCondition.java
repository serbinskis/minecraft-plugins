package net.minecraft.world.level.storage.loot.predicates;

public class AnyOfCondition extends CompositeLootItemCondition {

    AnyOfCondition(LootItemCondition[] alootitemcondition) {
        super(alootitemcondition, LootItemConditions.orConditions(alootitemcondition));
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
        protected LootItemCondition create(LootItemCondition[] alootitemcondition) {
            return new AnyOfCondition(alootitemcondition);
        }
    }

    public static class b extends CompositeLootItemCondition.b<AnyOfCondition> {

        public b() {}

        @Override
        protected AnyOfCondition create(LootItemCondition[] alootitemcondition) {
            return new AnyOfCondition(alootitemcondition);
        }
    }
}
