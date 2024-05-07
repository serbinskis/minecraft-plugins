package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItem extends LootSelectorEntry {

    public static final MapCodec<LootItem> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("name").forGetter((lootitem) -> {
            return lootitem.item;
        })).and(singletonFields(instance)).apply(instance, LootItem::new);
    });
    private final Holder<Item> item;

    private LootItem(Holder<Item> holder, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list1) {
        super(i, j, list, list1);
        this.item = holder;
    }

    @Override
    public LootEntryType getType() {
        return LootEntries.ITEM;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {
        consumer.accept(new ItemStack(this.item));
    }

    public static LootSelectorEntry.a<?> lootTableItem(IMaterial imaterial) {
        return simpleBuilder((i, j, list, list1) -> {
            return new LootItem(imaterial.asItem().builtInRegistryHolder(), i, j, list, list1);
        });
    }
}
