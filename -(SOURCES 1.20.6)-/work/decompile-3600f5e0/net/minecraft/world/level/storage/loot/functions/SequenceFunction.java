package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class SequenceFunction implements LootItemFunction {

    public static final MapCodec<SequenceFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LootItemFunctions.TYPED_CODEC.listOf().fieldOf("functions").forGetter((sequencefunction) -> {
            return sequencefunction.functions;
        })).apply(instance, SequenceFunction::new);
    });
    public static final Codec<SequenceFunction> INLINE_CODEC = LootItemFunctions.TYPED_CODEC.listOf().xmap(SequenceFunction::new, (sequencefunction) -> {
        return sequencefunction.functions;
    });
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootTableInfo, ItemStack> compositeFunction;

    private SequenceFunction(List<LootItemFunction> list) {
        this.functions = list;
        this.compositeFunction = LootItemFunctions.compose(list);
    }

    public static SequenceFunction of(List<LootItemFunction> list) {
        return new SequenceFunction(List.copyOf(list));
    }

    public ItemStack apply(ItemStack itemstack, LootTableInfo loottableinfo) {
        return (ItemStack) this.compositeFunction.apply(itemstack, loottableinfo);
    }

    @Override
    public void validate(LootCollector lootcollector) {
        LootItemFunction.super.validate(lootcollector);

        for (int i = 0; i < this.functions.size(); ++i) {
            ((LootItemFunction) this.functions.get(i)).validate(lootcollector.forChild(".function[" + i + "]"));
        }

    }

    @Override
    public LootItemFunctionType<SequenceFunction> getType() {
        return LootItemFunctions.SEQUENCE;
    }
}
