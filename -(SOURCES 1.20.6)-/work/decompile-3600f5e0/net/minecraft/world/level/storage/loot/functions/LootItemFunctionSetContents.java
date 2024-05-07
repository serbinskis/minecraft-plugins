package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.entries.LootEntries;
import net.minecraft.world.level.storage.loot.entries.LootEntryAbstract;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionSetContents extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionSetContents> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(ContainerComponentManipulators.CODEC.fieldOf("component").forGetter((lootitemfunctionsetcontents) -> {
            return lootitemfunctionsetcontents.component;
        }), LootEntries.CODEC.listOf().fieldOf("entries").forGetter((lootitemfunctionsetcontents) -> {
            return lootitemfunctionsetcontents.entries;
        }))).apply(instance, LootItemFunctionSetContents::new);
    });
    private final ContainerComponentManipulator<?> component;
    private final List<LootEntryAbstract> entries;

    LootItemFunctionSetContents(List<LootItemCondition> list, ContainerComponentManipulator<?> containercomponentmanipulator, List<LootEntryAbstract> list1) {
        super(list);
        this.component = containercomponentmanipulator;
        this.entries = List.copyOf(list1);
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSetContents> getType() {
        return LootItemFunctions.SET_CONTENTS;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.isEmpty()) {
            return itemstack;
        } else {
            Builder<ItemStack> builder = Stream.builder();

            this.entries.forEach((lootentryabstract) -> {
                lootentryabstract.expand(loottableinfo, (lootentry) -> {
                    WorldServer worldserver = loottableinfo.getLevel();

                    Objects.requireNonNull(builder);
                    lootentry.createItemStack(LootTable.createStackSplitter(worldserver, builder::add), loottableinfo);
                });
            });
            this.component.setContents(itemstack, builder.build());
            return itemstack;
        }
    }

    @Override
    public void validate(LootCollector lootcollector) {
        super.validate(lootcollector);

        for (int i = 0; i < this.entries.size(); ++i) {
            ((LootEntryAbstract) this.entries.get(i)).validate(lootcollector.forChild(".entry[" + i + "]"));
        }

    }

    public static LootItemFunctionSetContents.a setContents(ContainerComponentManipulator<?> containercomponentmanipulator) {
        return new LootItemFunctionSetContents.a(containercomponentmanipulator);
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionSetContents.a> {

        private final com.google.common.collect.ImmutableList.Builder<LootEntryAbstract> entries = ImmutableList.builder();
        private final ContainerComponentManipulator<?> component;

        public a(ContainerComponentManipulator<?> containercomponentmanipulator) {
            this.component = containercomponentmanipulator;
        }

        @Override
        protected LootItemFunctionSetContents.a getThis() {
            return this;
        }

        public LootItemFunctionSetContents.a withEntry(LootEntryAbstract.a<?> lootentryabstract_a) {
            this.entries.add(lootentryabstract_a.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionSetContents(this.getConditions(), this.component, this.entries.build());
        }
    }
}
