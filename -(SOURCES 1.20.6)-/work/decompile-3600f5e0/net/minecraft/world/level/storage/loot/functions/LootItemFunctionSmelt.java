package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.FurnaceRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootItemFunctionSmelt extends LootItemFunctionConditional {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<LootItemFunctionSmelt> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).apply(instance, LootItemFunctionSmelt::new);
    });

    private LootItemFunctionSmelt(List<LootItemCondition> list) {
        super(list);
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSmelt> getType() {
        return LootItemFunctions.FURNACE_SMELT;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.isEmpty()) {
            return itemstack;
        } else {
            Optional<RecipeHolder<FurnaceRecipe>> optional = loottableinfo.getLevel().getRecipeManager().getRecipeFor(Recipes.SMELTING, new InventorySubcontainer(new ItemStack[]{itemstack}), loottableinfo.getLevel());

            if (optional.isPresent()) {
                ItemStack itemstack1 = ((FurnaceRecipe) ((RecipeHolder) optional.get()).value()).getResultItem(loottableinfo.getLevel().registryAccess());

                if (!itemstack1.isEmpty()) {
                    return itemstack1.copyWithCount(itemstack.getCount());
                }
            }

            LootItemFunctionSmelt.LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", itemstack);
            return itemstack;
        }
    }

    public static LootItemFunctionConditional.a<?> smelted() {
        return simpleBuilder(LootItemFunctionSmelt::new);
    }
}
