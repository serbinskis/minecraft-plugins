package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.SystemUtils;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootEntries;
import net.minecraft.world.level.storage.loot.entries.LootEntry;
import net.minecraft.world.level.storage.loot.entries.LootEntryAbstract;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionUser;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionUser;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootSelector {

    public static final Codec<LootSelector> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(LootEntries.CODEC.listOf().fieldOf("entries").forGetter((lootselector) -> {
            return lootselector.entries;
        }), LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter((lootselector) -> {
            return lootselector.conditions;
        }), LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter((lootselector) -> {
            return lootselector.functions;
        }), NumberProviders.CODEC.fieldOf("rolls").forGetter((lootselector) -> {
            return lootselector.rolls;
        }), NumberProviders.CODEC.fieldOf("bonus_rolls").orElse(ConstantValue.exactly(0.0F)).forGetter((lootselector) -> {
            return lootselector.bonusRolls;
        })).apply(instance, LootSelector::new);
    });
    private final List<LootEntryAbstract> entries;
    private final List<LootItemCondition> conditions;
    private final Predicate<LootTableInfo> compositeCondition;
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootTableInfo, ItemStack> compositeFunction;
    private final NumberProvider rolls;
    private final NumberProvider bonusRolls;

    LootSelector(List<LootEntryAbstract> list, List<LootItemCondition> list1, List<LootItemFunction> list2, NumberProvider numberprovider, NumberProvider numberprovider1) {
        this.entries = list;
        this.conditions = list1;
        this.compositeCondition = SystemUtils.allOf(list1);
        this.functions = list2;
        this.compositeFunction = LootItemFunctions.compose(list2);
        this.rolls = numberprovider;
        this.bonusRolls = numberprovider1;
    }

    private void addRandomItem(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {
        RandomSource randomsource = loottableinfo.getRandom();
        List<LootEntry> list = Lists.newArrayList();
        MutableInt mutableint = new MutableInt();
        Iterator iterator = this.entries.iterator();

        while (iterator.hasNext()) {
            LootEntryAbstract lootentryabstract = (LootEntryAbstract) iterator.next();

            lootentryabstract.expand(loottableinfo, (lootentry) -> {
                int i = lootentry.getWeight(loottableinfo.getLuck());

                if (i > 0) {
                    list.add(lootentry);
                    mutableint.add(i);
                }

            });
        }

        int i = list.size();

        if (mutableint.intValue() != 0 && i != 0) {
            if (i == 1) {
                ((LootEntry) list.get(0)).createItemStack(consumer, loottableinfo);
            } else {
                int j = randomsource.nextInt(mutableint.intValue());
                Iterator iterator1 = list.iterator();

                LootEntry lootentry;

                do {
                    if (!iterator1.hasNext()) {
                        return;
                    }

                    lootentry = (LootEntry) iterator1.next();
                    j -= lootentry.getWeight(loottableinfo.getLuck());
                } while (j >= 0);

                lootentry.createItemStack(consumer, loottableinfo);
            }
        }
    }

    public void addRandomItems(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {
        if (this.compositeCondition.test(loottableinfo)) {
            Consumer<ItemStack> consumer1 = LootItemFunction.decorate(this.compositeFunction, consumer, loottableinfo);
            int i = this.rolls.getInt(loottableinfo) + MathHelper.floor(this.bonusRolls.getFloat(loottableinfo) * loottableinfo.getLuck());

            for (int j = 0; j < i; ++j) {
                this.addRandomItem(consumer1, loottableinfo);
            }

        }
    }

    public void validate(LootCollector lootcollector) {
        int i;

        for (i = 0; i < this.conditions.size(); ++i) {
            ((LootItemCondition) this.conditions.get(i)).validate(lootcollector.forChild(".condition[" + i + "]"));
        }

        for (i = 0; i < this.functions.size(); ++i) {
            ((LootItemFunction) this.functions.get(i)).validate(lootcollector.forChild(".functions[" + i + "]"));
        }

        for (i = 0; i < this.entries.size(); ++i) {
            ((LootEntryAbstract) this.entries.get(i)).validate(lootcollector.forChild(".entries[" + i + "]"));
        }

        this.rolls.validate(lootcollector.forChild(".rolls"));
        this.bonusRolls.validate(lootcollector.forChild(".bonusRolls"));
    }

    public static LootSelector.a lootPool() {
        return new LootSelector.a();
    }

    public static class a implements LootItemFunctionUser<LootSelector.a>, LootItemConditionUser<LootSelector.a> {

        private final Builder<LootEntryAbstract> entries = ImmutableList.builder();
        private final Builder<LootItemCondition> conditions = ImmutableList.builder();
        private final Builder<LootItemFunction> functions = ImmutableList.builder();
        private NumberProvider rolls = ConstantValue.exactly(1.0F);
        private NumberProvider bonusRolls = ConstantValue.exactly(0.0F);

        public a() {}

        public LootSelector.a setRolls(NumberProvider numberprovider) {
            this.rolls = numberprovider;
            return this;
        }

        @Override
        public LootSelector.a unwrap() {
            return this;
        }

        public LootSelector.a setBonusRolls(NumberProvider numberprovider) {
            this.bonusRolls = numberprovider;
            return this;
        }

        public LootSelector.a add(LootEntryAbstract.a<?> lootentryabstract_a) {
            this.entries.add(lootentryabstract_a.build());
            return this;
        }

        @Override
        public LootSelector.a when(LootItemCondition.a lootitemcondition_a) {
            this.conditions.add(lootitemcondition_a.build());
            return this;
        }

        @Override
        public LootSelector.a apply(LootItemFunction.a lootitemfunction_a) {
            this.functions.add(lootitemfunction_a.build());
            return this;
        }

        public LootSelector build() {
            return new LootSelector(this.entries.build(), this.conditions.build(), this.functions.build(), this.rolls, this.bonusRolls);
        }
    }
}
