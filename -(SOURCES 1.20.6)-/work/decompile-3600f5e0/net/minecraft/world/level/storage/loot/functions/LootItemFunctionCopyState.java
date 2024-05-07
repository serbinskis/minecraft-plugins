package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionCopyState extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionCopyState> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
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
    public LootItemFunctionType<LootItemFunctionCopyState> getType() {
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
            itemstack.update(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY, (blockitemstateproperties) -> {
                Iterator iterator = this.properties.iterator();

                while (iterator.hasNext()) {
                    IBlockState<?> iblockstate = (IBlockState) iterator.next();

                    if (iblockdata.hasProperty(iblockstate)) {
                        blockitemstateproperties = blockitemstateproperties.with(iblockstate, iblockdata);
                    }
                }

                return blockitemstateproperties;
            });
        }

        return itemstack;
    }

    public static LootItemFunctionCopyState.a copyState(Block block) {
        return new LootItemFunctionCopyState.a(block);
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionCopyState.a> {

        private final Holder<Block> block;
        private final Builder<IBlockState<?>> properties = ImmutableSet.builder();

        a(Block block) {
            this.block = block.builtInRegistryHolder();
        }

        public LootItemFunctionCopyState.a copy(IBlockState<?> iblockstate) {
            if (!((Block) this.block.value()).getStateDefinition().getProperties().contains(iblockstate)) {
                String s = String.valueOf(iblockstate);

                throw new IllegalStateException("Property " + s + " is not present on block " + String.valueOf(this.block));
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
