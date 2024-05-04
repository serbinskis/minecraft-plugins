package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootSelectorEmpty extends LootSelectorEntry {

    public static final MapCodec<LootSelectorEmpty> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return singletonFields(instance).apply(instance, LootSelectorEmpty::new);
    });

    private LootSelectorEmpty(int i, int j, List<LootItemCondition> list, List<LootItemFunction> list1) {
        super(i, j, list, list1);
    }

    @Override
    public LootEntryType getType() {
        return LootEntries.EMPTY;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {}

    public static LootSelectorEntry.a<?> emptyItem() {
        return simpleBuilder(LootSelectorEmpty::new);
    }
}
