package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetItemFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetItemFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(RegistryFixedCodec.create(Registries.ITEM).fieldOf("item").forGetter((setitemfunction) -> {
            return setitemfunction.item;
        })).apply(instance, SetItemFunction::new);
    });
    private final Holder<Item> item;

    private SetItemFunction(List<LootItemCondition> list, Holder<Item> holder) {
        super(list);
        this.item = holder;
    }

    @Override
    public LootItemFunctionType<SetItemFunction> getType() {
        return LootItemFunctions.SET_ITEM;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        return itemstack.transmuteCopy((IMaterial) this.item.value(), itemstack.getCount());
    }
}
