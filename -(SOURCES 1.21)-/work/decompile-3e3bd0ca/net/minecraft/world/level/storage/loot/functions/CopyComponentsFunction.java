package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.SystemUtils;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.INamable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyComponentsFunction extends LootItemFunctionConditional {

    public static final MapCodec<CopyComponentsFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(CopyComponentsFunction.b.CODEC.fieldOf("source").forGetter((copycomponentsfunction) -> {
            return copycomponentsfunction.source;
        }), DataComponentType.CODEC.listOf().optionalFieldOf("include").forGetter((copycomponentsfunction) -> {
            return copycomponentsfunction.include;
        }), DataComponentType.CODEC.listOf().optionalFieldOf("exclude").forGetter((copycomponentsfunction) -> {
            return copycomponentsfunction.exclude;
        }))).apply(instance, CopyComponentsFunction::new);
    });
    private final CopyComponentsFunction.b source;
    private final Optional<List<DataComponentType<?>>> include;
    private final Optional<List<DataComponentType<?>>> exclude;
    private final Predicate<DataComponentType<?>> bakedPredicate;

    CopyComponentsFunction(List<LootItemCondition> list, CopyComponentsFunction.b copycomponentsfunction_b, Optional<List<DataComponentType<?>>> optional, Optional<List<DataComponentType<?>>> optional1) {
        super(list);
        this.source = copycomponentsfunction_b;
        this.include = optional.map(List::copyOf);
        this.exclude = optional1.map(List::copyOf);
        List<Predicate<DataComponentType<?>>> list1 = new ArrayList(2);

        optional1.ifPresent((list2) -> {
            list1.add((datacomponenttype) -> {
                return !list2.contains(datacomponenttype);
            });
        });
        optional.ifPresent((list2) -> {
            Objects.requireNonNull(list2);
            list1.add(list2::contains);
        });
        this.bakedPredicate = SystemUtils.allOf(list1);
    }

    @Override
    public LootItemFunctionType<CopyComponentsFunction> getType() {
        return LootItemFunctions.COPY_COMPONENTS;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.source.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        DataComponentMap datacomponentmap = this.source.get(loottableinfo);

        itemstack.applyComponents(datacomponentmap.filter(this.bakedPredicate));
        return itemstack;
    }

    public static CopyComponentsFunction.a copyComponents(CopyComponentsFunction.b copycomponentsfunction_b) {
        return new CopyComponentsFunction.a(copycomponentsfunction_b);
    }

    public static enum b implements INamable {

        BLOCK_ENTITY("block_entity");

        public static final Codec<CopyComponentsFunction.b> CODEC = INamable.fromValues(CopyComponentsFunction.b::values);
        private final String name;

        private b(final String s) {
            this.name = s;
        }

        public DataComponentMap get(LootTableInfo loottableinfo) {
            switch (this.ordinal()) {
                case 0:
                    TileEntity tileentity = (TileEntity) loottableinfo.getParamOrNull(LootContextParameters.BLOCK_ENTITY);

                    return tileentity != null ? tileentity.collectComponents() : DataComponentMap.EMPTY;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }

        public Set<LootContextParameter<?>> getReferencedContextParams() {
            switch (this.ordinal()) {
                case 0:
                    return Set.of(LootContextParameters.BLOCK_ENTITY);
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static class a extends LootItemFunctionConditional.a<CopyComponentsFunction.a> {

        private final CopyComponentsFunction.b source;
        private Optional<Builder<DataComponentType<?>>> include = Optional.empty();
        private Optional<Builder<DataComponentType<?>>> exclude = Optional.empty();

        a(CopyComponentsFunction.b copycomponentsfunction_b) {
            this.source = copycomponentsfunction_b;
        }

        public CopyComponentsFunction.a include(DataComponentType<?> datacomponenttype) {
            if (this.include.isEmpty()) {
                this.include = Optional.of(ImmutableList.builder());
            }

            ((Builder) this.include.get()).add(datacomponenttype);
            return this;
        }

        public CopyComponentsFunction.a exclude(DataComponentType<?> datacomponenttype) {
            if (this.exclude.isEmpty()) {
                this.exclude = Optional.of(ImmutableList.builder());
            }

            ((Builder) this.exclude.get()).add(datacomponenttype);
            return this;
        }

        @Override
        protected CopyComponentsFunction.a getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyComponentsFunction(this.getConditions(), this.source, this.include.map(Builder::build), this.exclude.map(Builder::build));
        }
    }
}
