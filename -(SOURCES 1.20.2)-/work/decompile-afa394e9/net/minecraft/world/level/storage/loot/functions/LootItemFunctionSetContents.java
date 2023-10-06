package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.entries.LootEntries;
import net.minecraft.world.level.storage.loot.entries.LootEntryAbstract;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionSetContents extends LootItemFunctionConditional {

    public static final Codec<LootItemFunctionSetContents> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter((lootitemfunctionsetcontents) -> {
            return lootitemfunctionsetcontents.type;
        }), LootEntries.CODEC.listOf().fieldOf("entries").forGetter((lootitemfunctionsetcontents) -> {
            return lootitemfunctionsetcontents.entries;
        }))).apply(instance, LootItemFunctionSetContents::new);
    });
    private final Holder<TileEntityTypes<?>> type;
    private final List<LootEntryAbstract> entries;

    LootItemFunctionSetContents(List<LootItemCondition> list, Holder<TileEntityTypes<?>> holder, List<LootEntryAbstract> list1) {
        super(list);
        this.type = holder;
        this.entries = List.copyOf(list1);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_CONTENTS;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.isEmpty()) {
            return itemstack;
        } else {
            NonNullList<ItemStack> nonnulllist = NonNullList.create();

            this.entries.forEach((lootentryabstract) -> {
                lootentryabstract.expand(loottableinfo, (lootentry) -> {
                    WorldServer worldserver = loottableinfo.getLevel();

                    Objects.requireNonNull(nonnulllist);
                    lootentry.createItemStack(LootTable.createStackSplitter(worldserver, nonnulllist::add), loottableinfo);
                });
            });
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            ContainerUtil.saveAllItems(nbttagcompound, nonnulllist);
            NBTTagCompound nbttagcompound1 = ItemBlock.getBlockEntityData(itemstack);

            if (nbttagcompound1 == null) {
                nbttagcompound1 = nbttagcompound;
            } else {
                nbttagcompound1.merge(nbttagcompound);
            }

            ItemBlock.setBlockEntityData(itemstack, (TileEntityTypes) this.type.value(), nbttagcompound1);
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

    public static LootItemFunctionSetContents.a setContents(TileEntityTypes<?> tileentitytypes) {
        return new LootItemFunctionSetContents.a(tileentitytypes);
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionSetContents.a> {

        private final Builder<LootEntryAbstract> entries = ImmutableList.builder();
        private final TileEntityTypes<?> type;

        public a(TileEntityTypes<?> tileentitytypes) {
            this.type = tileentitytypes;
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
            return new LootItemFunctionSetContents(this.getConditions(), this.type.builtInRegistryHolder(), this.entries.build());
        }
    }
}
