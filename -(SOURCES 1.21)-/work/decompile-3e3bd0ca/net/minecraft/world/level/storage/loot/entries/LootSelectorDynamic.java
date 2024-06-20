package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootSelectorDynamic extends LootSelectorEntry {

    public static final MapCodec<LootSelectorDynamic> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("name").forGetter((lootselectordynamic) -> {
            return lootselectordynamic.name;
        })).and(singletonFields(instance)).apply(instance, LootSelectorDynamic::new);
    });
    private final MinecraftKey name;

    private LootSelectorDynamic(MinecraftKey minecraftkey, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list1) {
        super(i, j, list, list1);
        this.name = minecraftkey;
    }

    @Override
    public LootEntryType getType() {
        return LootEntries.DYNAMIC;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {
        loottableinfo.addDynamicDrops(this.name, consumer);
    }

    public static LootSelectorEntry.a<?> dynamicEntry(MinecraftKey minecraftkey) {
        return simpleBuilder((i, j, list, list1) -> {
            return new LootSelectorDynamic(minecraftkey, i, j, list, list1);
        });
    }
}
