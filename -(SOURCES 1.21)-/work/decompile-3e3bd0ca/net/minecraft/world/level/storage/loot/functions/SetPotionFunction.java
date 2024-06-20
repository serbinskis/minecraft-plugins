package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetPotionFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(PotionRegistry.CODEC.fieldOf("id").forGetter((setpotionfunction) -> {
            return setpotionfunction.potion;
        })).apply(instance, SetPotionFunction::new);
    });
    private final Holder<PotionRegistry> potion;

    private SetPotionFunction(List<LootItemCondition> list, Holder<PotionRegistry> holder) {
        super(list);
        this.potion = holder;
    }

    @Override
    public LootItemFunctionType<SetPotionFunction> getType() {
        return LootItemFunctions.SET_POTION;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, this.potion, PotionContents::withPotion);
        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> setPotion(Holder<PotionRegistry> holder) {
        return simpleBuilder((list) -> {
            return new SetPotionFunction(list, holder);
        });
    }
}
