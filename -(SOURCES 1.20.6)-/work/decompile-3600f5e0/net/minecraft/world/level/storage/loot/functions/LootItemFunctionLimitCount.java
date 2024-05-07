package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionLimitCount extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionLimitCount> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(IntRange.CODEC.fieldOf("limit").forGetter((lootitemfunctionlimitcount) -> {
            return lootitemfunctionlimitcount.limiter;
        })).apply(instance, LootItemFunctionLimitCount::new);
    });
    private final IntRange limiter;

    private LootItemFunctionLimitCount(List<LootItemCondition> list, IntRange intrange) {
        super(list);
        this.limiter = intrange;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionLimitCount> getType() {
        return LootItemFunctions.LIMIT_COUNT;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.limiter.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        int i = this.limiter.clamp(loottableinfo, itemstack.getCount());

        itemstack.setCount(i);
        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> limitCount(IntRange intrange) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionLimitCount(list, intrange);
        });
    }
}
