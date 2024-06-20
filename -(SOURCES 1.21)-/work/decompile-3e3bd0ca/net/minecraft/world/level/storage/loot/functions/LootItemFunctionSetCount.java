package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class LootItemFunctionSetCount extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionSetCount> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(NumberProviders.CODEC.fieldOf("count").forGetter((lootitemfunctionsetcount) -> {
            return lootitemfunctionsetcount.value;
        }), Codec.BOOL.fieldOf("add").orElse(false).forGetter((lootitemfunctionsetcount) -> {
            return lootitemfunctionsetcount.add;
        }))).apply(instance, LootItemFunctionSetCount::new);
    });
    private final NumberProvider value;
    private final boolean add;

    private LootItemFunctionSetCount(List<LootItemCondition> list, NumberProvider numberprovider, boolean flag) {
        super(list);
        this.value = numberprovider;
        this.add = flag;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSetCount> getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        int i = this.add ? itemstack.getCount() : 0;

        itemstack.setCount(i + this.value.getInt(loottableinfo));
        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> setCount(NumberProvider numberprovider) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetCount(list, numberprovider, false);
        });
    }

    public static LootItemFunctionConditional.a<?> setCount(NumberProvider numberprovider, boolean flag) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetCount(list, numberprovider, flag);
        });
    }
}
