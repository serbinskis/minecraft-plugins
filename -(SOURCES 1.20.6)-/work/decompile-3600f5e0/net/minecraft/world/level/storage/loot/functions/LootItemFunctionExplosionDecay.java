package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionExplosionDecay extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionExplosionDecay> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).apply(instance, LootItemFunctionExplosionDecay::new);
    });

    private LootItemFunctionExplosionDecay(List<LootItemCondition> list) {
        super(list);
    }

    @Override
    public LootItemFunctionType<LootItemFunctionExplosionDecay> getType() {
        return LootItemFunctions.EXPLOSION_DECAY;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        Float ofloat = (Float) loottableinfo.getParamOrNull(LootContextParameters.EXPLOSION_RADIUS);

        if (ofloat != null) {
            RandomSource randomsource = loottableinfo.getRandom();
            float f = 1.0F / ofloat;
            int i = itemstack.getCount();
            int j = 0;

            for (int k = 0; k < i; ++k) {
                if (randomsource.nextFloat() <= f) {
                    ++j;
                }
            }

            itemstack.setCount(j);
        }

        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> explosionDecay() {
        return simpleBuilder(LootItemFunctionExplosionDecay::new);
    }
}
