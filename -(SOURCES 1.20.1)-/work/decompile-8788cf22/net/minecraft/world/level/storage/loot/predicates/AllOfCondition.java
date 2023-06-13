package net.minecraft.world.level.storage.loot.predicates;

public class AllOfCondition extends CompositeLootItemCondition {

    AllOfCondition(LootItemCondition[] alootitemcondition) {
        super(alootitemcondition, LootItemConditions.andConditions(alootitemcondition));
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
        protected LootItemCondition create(LootItemCondition[] alootitemcondition) {
            return new AllOfCondition(alootitemcondition);
        }
    }

    public static class b extends CompositeLootItemCondition.b<AllOfCondition> {

        public b() {}

        @Override
        protected AllOfCondition create(LootItemCondition[] alootitemcondition) {
            return new AllOfCondition(alootitemcondition);
        }
    }
}
