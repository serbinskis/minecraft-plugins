package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public abstract class CompositeLootItemCondition implements LootItemCondition {

    protected final List<LootItemCondition> terms;
    private final Predicate<LootTableInfo> composedPredicate;

    protected CompositeLootItemCondition(List<LootItemCondition> list, Predicate<LootTableInfo> predicate) {
        this.terms = list;
        this.composedPredicate = predicate;
    }

    protected static <T extends CompositeLootItemCondition> MapCodec<T> createCodec(Function<List<LootItemCondition>, T> function) {
        return RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(LootItemCondition.DIRECT_CODEC.listOf().fieldOf("terms").forGetter((compositelootitemcondition) -> {
                return compositelootitemcondition.terms;
            })).apply(instance, function);
        });
    }

    protected static <T extends CompositeLootItemCondition> Codec<T> createInlineCodec(Function<List<LootItemCondition>, T> function) {
        return LootItemCondition.DIRECT_CODEC.listOf().xmap(function, (compositelootitemcondition) -> {
            return compositelootitemcondition.terms;
        });
    }

    public final boolean test(LootTableInfo loottableinfo) {
        return this.composedPredicate.test(loottableinfo);
    }

    @Override
    public void validate(LootCollector lootcollector) {
        LootItemCondition.super.validate(lootcollector);

        for (int i = 0; i < this.terms.size(); ++i) {
            ((LootItemCondition) this.terms.get(i)).validate(lootcollector.forChild(".term[" + i + "]"));
        }

    }

    public abstract static class a implements LootItemCondition.a {

        private final Builder<LootItemCondition> terms = ImmutableList.builder();

        protected a(LootItemCondition.a... alootitemcondition_a) {
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
            return this.create(this.terms.build());
        }

        protected abstract LootItemCondition create(List<LootItemCondition> list);
    }
}
