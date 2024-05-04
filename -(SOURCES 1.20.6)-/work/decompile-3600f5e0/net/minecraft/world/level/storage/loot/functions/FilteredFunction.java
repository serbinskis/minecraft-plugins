package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.advancements.critereon.CriterionConditionItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FilteredFunction extends LootItemFunctionConditional {

    public static final MapCodec<FilteredFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(CriterionConditionItem.CODEC.fieldOf("item_filter").forGetter((filteredfunction) -> {
            return filteredfunction.filter;
        }), LootItemFunctions.ROOT_CODEC.fieldOf("modifier").forGetter((filteredfunction) -> {
            return filteredfunction.modifier;
        }))).apply(instance, FilteredFunction::new);
    });
    private final CriterionConditionItem filter;
    private final LootItemFunction modifier;

    private FilteredFunction(List<LootItemCondition> list, CriterionConditionItem criterionconditionitem, LootItemFunction lootitemfunction) {
        super(list);
        this.filter = criterionconditionitem;
        this.modifier = lootitemfunction;
    }

    @Override
    public LootItemFunctionType<FilteredFunction> getType() {
        return LootItemFunctions.FILTERED;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        return this.filter.test(itemstack) ? (ItemStack) this.modifier.apply(itemstack, loottableinfo) : itemstack;
    }

    @Override
    public void validate(LootCollector lootcollector) {
        super.validate(lootcollector);
        this.modifier.validate(lootcollector.forChild(".modifier"));
    }
}
