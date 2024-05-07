package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootEntryGroup extends LootEntryChildrenAbstract {

    public static final MapCodec<LootEntryGroup> CODEC = createCodec(LootEntryGroup::new);

    LootEntryGroup(List<LootEntryAbstract> list, List<LootItemCondition> list1) {
        super(list, list1);
    }

    @Override
    public LootEntryType getType() {
        return LootEntries.GROUP;
    }

    @Override
    protected LootEntryChildren compose(List<? extends LootEntryChildren> list) {
        LootEntryChildren lootentrychildren;

        switch (list.size()) {
            case 0:
                lootentrychildren = LootEntryGroup.ALWAYS_TRUE;
                break;
            case 1:
                lootentrychildren = (LootEntryChildren) list.get(0);
                break;
            case 2:
                LootEntryChildren lootentrychildren1 = (LootEntryChildren) list.get(0);
                LootEntryChildren lootentrychildren2 = (LootEntryChildren) list.get(1);

                lootentrychildren = (loottableinfo, consumer) -> {
                    lootentrychildren1.expand(loottableinfo, consumer);
                    lootentrychildren2.expand(loottableinfo, consumer);
                    return true;
                };
                break;
            default:
                lootentrychildren = (loottableinfo, consumer) -> {
                    Iterator iterator = list.iterator();

                    while (iterator.hasNext()) {
                        LootEntryChildren lootentrychildren3 = (LootEntryChildren) iterator.next();

                        lootentrychildren3.expand(loottableinfo, consumer);
                    }

                    return true;
                };
        }

        return lootentrychildren;
    }

    public static LootEntryGroup.a list(LootEntryAbstract.a<?>... alootentryabstract_a) {
        return new LootEntryGroup.a(alootentryabstract_a);
    }

    public static class a extends LootEntryAbstract.a<LootEntryGroup.a> {

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
        protected LootEntryGroup.a getThis() {
            return this;
        }

        @Override
        public LootEntryGroup.a append(LootEntryAbstract.a<?> lootentryabstract_a) {
            this.entries.add(lootentryabstract_a.build());
            return this;
        }

        @Override
        public LootEntryAbstract build() {
            return new LootEntryGroup(this.entries.build(), this.getConditions());
        }
    }
}
