package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootEntryAlternatives extends LootEntryChildrenAbstract {

    public static final MapCodec<LootEntryAlternatives> CODEC = createCodec(LootEntryAlternatives::new);

    LootEntryAlternatives(List<LootEntryAbstract> list, List<LootItemCondition> list1) {
        super(list, list1);
    }

    @Override
    public LootEntryType getType() {
        return LootEntries.ALTERNATIVES;
    }

    @Override
    protected LootEntryChildren compose(List<? extends LootEntryChildren> list) {
        LootEntryChildren lootentrychildren;

        switch (list.size()) {
            case 0:
                lootentrychildren = LootEntryAlternatives.ALWAYS_FALSE;
                break;
            case 1:
                lootentrychildren = (LootEntryChildren) list.get(0);
                break;
            case 2:
                lootentrychildren = ((LootEntryChildren) list.get(0)).or((LootEntryChildren) list.get(1));
                break;
            default:
                lootentrychildren = (loottableinfo, consumer) -> {
                    Iterator iterator = list.iterator();

                    LootEntryChildren lootentrychildren1;

                    do {
                        if (!iterator.hasNext()) {
                            return false;
                        }

                        lootentrychildren1 = (LootEntryChildren) iterator.next();
                    } while (!lootentrychildren1.expand(loottableinfo, consumer));

                    return true;
                };
        }

        return lootentrychildren;
    }

    @Override
    public void validate(LootCollector lootcollector) {
        super.validate(lootcollector);

        for (int i = 0; i < this.children.size() - 1; ++i) {
            if (((LootEntryAbstract) this.children.get(i)).conditions.isEmpty()) {
                lootcollector.reportProblem("Unreachable entry!");
            }
        }

    }

    public static LootEntryAlternatives.a alternatives(LootEntryAbstract.a<?>... alootentryabstract_a) {
        return new LootEntryAlternatives.a(alootentryabstract_a);
    }

    public static <E> LootEntryAlternatives.a alternatives(Collection<E> collection, Function<E, LootEntryAbstract.a<?>> function) {
        Stream stream = collection.stream();

        Objects.requireNonNull(function);
        return new LootEntryAlternatives.a((LootEntryAbstract.a[]) stream.map(function::apply).toArray((i) -> {
            return new LootEntryAbstract.a[i];
        }));
    }

    public static class a extends LootEntryAbstract.a<LootEntryAlternatives.a> {

        private final Builder<LootEntryAbstract> entries = ImmutableList.builder();

        public a(LootEntryAbstract.a<?>... alootentryabstract_a) {
            LootEntryAbstract.a[] alootentryabstract_a1 = alootentryabstract_a;
            int i = alootentryabstract_a.length;

            for (int j = 0; j < i; ++j) {
                LootEntryAbstract.a<?> lootentryabstract_a = alootentryabstract_a1[j];

                this.entries.add(lootentryabstract_a.build());
            }

        }

        @Override
        protected LootEntryAlternatives.a getThis() {
            return this;
        }

        @Override
        public LootEntryAlternatives.a otherwise(LootEntryAbstract.a<?> lootentryabstract_a) {
            this.entries.add(lootentryabstract_a.build());
            return this;
        }

        @Override
        public LootEntryAbstract build() {
            return new LootEntryAlternatives(this.entries.build(), this.getConditions());
        }
    }
}
