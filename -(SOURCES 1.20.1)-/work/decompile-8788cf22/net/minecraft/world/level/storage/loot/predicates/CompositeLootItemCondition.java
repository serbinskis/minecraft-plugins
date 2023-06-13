package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootSerializer;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public abstract class CompositeLootItemCondition implements LootItemCondition {

    final LootItemCondition[] terms;
    private final Predicate<LootTableInfo> composedPredicate;

    protected CompositeLootItemCondition(LootItemCondition[] alootitemcondition, Predicate<LootTableInfo> predicate) {
        this.terms = alootitemcondition;
        this.composedPredicate = predicate;
    }

    public final boolean test(LootTableInfo loottableinfo) {
        return this.composedPredicate.test(loottableinfo);
    }

    @Override
    public void validate(LootCollector lootcollector) {
        LootItemCondition.super.validate(lootcollector);

        for (int i = 0; i < this.terms.length; ++i) {
            this.terms[i].validate(lootcollector.forChild(".term[" + i + "]"));
        }

    }

    public abstract static class b<T extends CompositeLootItemCondition> implements LootSerializer<T> {

        public b() {}

        public void serialize(JsonObject jsonobject, CompositeLootItemCondition compositelootitemcondition, JsonSerializationContext jsonserializationcontext) {
            jsonobject.add("terms", jsonserializationcontext.serialize(compositelootitemcondition.terms));
        }

        @Override
        public T deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
            LootItemCondition[] alootitemcondition = (LootItemCondition[]) ChatDeserializer.getAsObject(jsonobject, "terms", jsondeserializationcontext, LootItemCondition[].class);

            return this.create(alootitemcondition);
        }

        protected abstract T create(LootItemCondition[] alootitemcondition);
    }

    public abstract static class a implements LootItemCondition.a {

        private final List<LootItemCondition> terms = new ArrayList();

        public a(LootItemCondition.a... alootitemcondition_a) {
            LootItemCondition.a[] alootitemcondition_a1 = alootitemcondition_a;
            int i = alootitemcondition_a.length;

            for (int j = 0; j < i; ++j) {
                LootItemCondition.a lootitemcondition_a = alootitemcondition_a1[j];

                this.terms.add(lootitemcondition_a.build());
            }

        }

        public void addTerm(LootItemCondition.a lootitemcondition_a) {
            this.terms.add(lootitemcondition_a.build());
        }

        @Override
        public LootItemCondition build() {
            LootItemCondition[] alootitemcondition = (LootItemCondition[]) this.terms.toArray((i) -> {
                return new LootItemCondition[i];
            });

            return this.create(alootitemcondition);
        }

        protected abstract LootItemCondition create(LootItemCondition[] alootitemcondition);
    }
}
