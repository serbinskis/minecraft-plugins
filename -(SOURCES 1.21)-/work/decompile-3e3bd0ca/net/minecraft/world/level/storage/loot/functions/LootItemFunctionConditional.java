package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.SystemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionUser;

public abstract class LootItemFunctionConditional implements LootItemFunction {

    protected final List<LootItemCondition> predicates;
    private final Predicate<LootTableInfo> compositePredicates;

    protected LootItemFunctionConditional(List<LootItemCondition> list) {
        this.predicates = list;
        this.compositePredicates = SystemUtils.allOf(list);
    }

    @Override
    public abstract LootItemFunctionType<? extends LootItemFunctionConditional> getType();

    protected static <T extends LootItemFunctionConditional> P1<Mu<T>, List<LootItemCondition>> commonFields(Instance<T> instance) {
        return instance.group(LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter((lootitemfunctionconditional) -> {
            return lootitemfunctionconditional.predicates;
        }));
    }

    public final ItemStack apply(ItemStack itemstack, LootTableInfo loottableinfo) {
        return this.compositePredicates.test(loottableinfo) ? this.run(itemstack, loottableinfo) : itemstack;
    }

    protected abstract ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo);

    @Override
    public void validate(LootCollector lootcollector) {
        LootItemFunction.super.validate(lootcollector);

        for (int i = 0; i < this.predicates.size(); ++i) {
            ((LootItemCondition) this.predicates.get(i)).validate(lootcollector.forChild(".conditions[" + i + "]"));
        }

    }

    protected static LootItemFunctionConditional.a<?> simpleBuilder(Function<List<LootItemCondition>, LootItemFunction> function) {
        return new LootItemFunctionConditional.b(function);
    }

    private static final class b extends LootItemFunctionConditional.a<LootItemFunctionConditional.b> {

        private final Function<List<LootItemCondition>, LootItemFunction> constructor;

        public b(Function<List<LootItemCondition>, LootItemFunction> function) {
            this.constructor = function;
        }

        @Override
        protected LootItemFunctionConditional.b getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return (LootItemFunction) this.constructor.apply(this.getConditions());
        }
    }

    public abstract static class a<T extends LootItemFunctionConditional.a<T>> implements LootItemFunction.a, LootItemConditionUser<T> {

        private final Builder<LootItemCondition> conditions = ImmutableList.builder();

        public a() {}

        @Override
        public T when(LootItemCondition.a lootitemcondition_a) {
            this.conditions.add(lootitemcondition_a.build());
            return this.getThis();
        }

        @Override
        public final T unwrap() {
            return this.getThis();
        }

        protected abstract T getThis();

        protected List<LootItemCondition> getConditions() {
            return this.conditions.build();
        }
    }
}
