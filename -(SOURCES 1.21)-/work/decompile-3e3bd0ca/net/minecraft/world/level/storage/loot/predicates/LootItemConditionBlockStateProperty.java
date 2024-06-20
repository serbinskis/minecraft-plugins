package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.CriterionTriggerProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public record LootItemConditionBlockStateProperty(Holder<Block> block, Optional<CriterionTriggerProperties> properties) implements LootItemCondition {

    public static final MapCodec<LootItemConditionBlockStateProperty> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(LootItemConditionBlockStateProperty::block), CriterionTriggerProperties.CODEC.optionalFieldOf("properties").forGetter(LootItemConditionBlockStateProperty::properties)).apply(instance, LootItemConditionBlockStateProperty::new);
    }).validate(LootItemConditionBlockStateProperty::validate);

    private static DataResult<LootItemConditionBlockStateProperty> validate(LootItemConditionBlockStateProperty lootitemconditionblockstateproperty) {
        return (DataResult) lootitemconditionblockstateproperty.properties().flatMap((criteriontriggerproperties) -> {
            return criteriontriggerproperties.checkState(((Block) lootitemconditionblockstateproperty.block().value()).getStateDefinition());
        }).map((s) -> {
            return DataResult.error(() -> {
                String s1 = String.valueOf(lootitemconditionblockstateproperty.block());

                return "Block " + s1 + " has no property" + s;
            });
        }).orElse(DataResult.success(lootitemconditionblockstateproperty));
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.BLOCK_STATE_PROPERTY;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return Set.of(LootContextParameters.BLOCK_STATE);
    }

    public boolean test(LootTableInfo loottableinfo) {
        IBlockData iblockdata = (IBlockData) loottableinfo.getParamOrNull(LootContextParameters.BLOCK_STATE);

        return iblockdata != null && iblockdata.is(this.block) && (this.properties.isEmpty() || ((CriterionTriggerProperties) this.properties.get()).matches(iblockdata));
    }

    public static LootItemConditionBlockStateProperty.a hasBlockStateProperties(Block block) {
        return new LootItemConditionBlockStateProperty.a(block);
    }

    public static class a implements LootItemCondition.a {

        private final Holder<Block> block;
        private Optional<CriterionTriggerProperties> properties = Optional.empty();

        public a(Block block) {
            this.block = block.builtInRegistryHolder();
        }

        public LootItemConditionBlockStateProperty.a setProperties(CriterionTriggerProperties.a criteriontriggerproperties_a) {
            this.properties = criteriontriggerproperties_a.build();
            return this;
        }

        @Override
        public LootItemCondition build() {
            return new LootItemConditionBlockStateProperty(this.block, this.properties);
        }
    }
}
