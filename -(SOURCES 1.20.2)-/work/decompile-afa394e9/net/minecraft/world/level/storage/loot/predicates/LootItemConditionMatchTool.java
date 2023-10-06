package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.CriterionConditionItem;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public record LootItemConditionMatchTool(Optional<CriterionConditionItem> predicate) implements LootItemCondition {

    public static final Codec<LootItemConditionMatchTool> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "predicate").forGetter(LootItemConditionMatchTool::predicate)).apply(instance, LootItemConditionMatchTool::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.MATCH_TOOL;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParameters.TOOL);
    }

    public boolean test(LootTableInfo loottableinfo) {
        ItemStack itemstack = (ItemStack) loottableinfo.getParamOrNull(LootContextParameters.TOOL);

        return itemstack != null && (this.predicate.isEmpty() || ((CriterionConditionItem) this.predicate.get()).matches(itemstack));
    }

    public static LootItemCondition.a toolMatches(CriterionConditionItem.a criterionconditionitem_a) {
        return () -> {
            return new LootItemConditionMatchTool(Optional.of(criterionconditionitem_a.build()));
        };
    }
}
