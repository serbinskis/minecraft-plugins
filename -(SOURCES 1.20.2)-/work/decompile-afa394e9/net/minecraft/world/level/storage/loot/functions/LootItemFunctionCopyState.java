package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionCopyState extends LootItemFunctionConditional {

    public static final Codec<LootItemFunctionCopyState> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter((lootitemfunctioncopystate) -> {
            return lootitemfunctioncopystate.block;
        }), Codec.STRING.listOf().fieldOf("properties").forGetter((lootitemfunctioncopystate) -> {
            return lootitemfunctioncopystate.properties.stream().map(IBlockState::getName).toList();
        }))).apply(instance, LootItemFunctionCopyState::new);
    });
    private final Holder<Block> block;
    private final Set<IBlockState<?>> properties;

    LootItemFunctionCopyState(List<LootItemCondition> list, Holder<Block> holder, Set<IBlockState<?>> set) {
        super(list);
        this.block = holder;
        this.properties = set;
    }

    private LootItemFunctionCopyState(List<LootItemCondition> list, Holder<Block> holder, List<String> list1) {
        Stream stream = list1.stream();
        BlockStateList blockstatelist = ((Block) holder.value()).getStateDefinition();

        Objects.requireNonNull(blockstatelist);
        this(list, holder, (Set) stream.map(blockstatelist::getProperty).filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_STATE;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParameters.BLOCK_STATE);
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        IBlockData iblockdata = (IBlockData) loottableinfo.getParamOrNull(LootContextParameters.BLOCK_STATE);

        if (iblockdata != null) {
            NBTTagCompound nbttagcompound = itemstack.getOrCreateTag();
            NBTTagCompound nbttagcompound1;

            if (nbttagcompound.contains("BlockStateTag", 10)) {
                nbttagcompound1 = nbttagcompound.getCompound("BlockStateTag");
            } else {
                nbttagcompound1 = new NBTTagCompound();
                nbttagcompound.put("BlockStateTag", nbttagcompound1);
            }

            Iterator iterator = this.properties.iterator();

            while (iterator.hasNext()) {
                IBlockState<?> iblockstate = (IBlockState) iterator.next();

                if (iblockdata.hasProperty(iblockstate)) {
                    nbttagcompound1.putString(iblockstate.getName(), serialize(iblockdata, iblockstate));
                }
            }
        }

        return itemstack;
    }

    public static LootItemFunctionCopyState.a copyState(Block block) {
        return new LootItemFunctionCopyState.a(block);
    }

    private static <T extends Comparable<T>> String serialize(IBlockData iblockdata, IBlockState<T> iblockstate) {
        T t0 = iblockdata.getValue(iblockstate);

        return iblockstate.getName(t0);
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionCopyState.a> {

        private final Holder<Block> block;
        private final Builder<IBlockState<?>> properties = ImmutableSet.builder();

        a(Block block) {
            this.block = block.builtInRegistryHolder();
        }

        public LootItemFunctionCopyState.a copy(IBlockState<?> iblockstate) {
            if (!((Block) this.block.value()).getStateDefinition().getProperties().contains(iblockstate)) {
                throw new IllegalStateException("Property " + iblockstate + " is not present on block " + this.block);
            } else {
                this.properties.add(iblockstate);
                return this;
            }
        }

        @Override
        protected LootItemFunctionCopyState.a getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionCopyState(this.getConditions(), this.block, this.properties.build());
        }
    }
}
