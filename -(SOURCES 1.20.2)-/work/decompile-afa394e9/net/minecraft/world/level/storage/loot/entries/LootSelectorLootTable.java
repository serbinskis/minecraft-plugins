package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootSelectorLootTable extends LootSelectorEntry {

    public static final Codec<LootSelectorLootTable> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("name").forGetter((lootselectorloottable) -> {
            return lootselectorloottable.name;
        })).and(singletonFields(instance)).apply(instance, LootSelectorLootTable::new);
    });
    private final MinecraftKey name;

    private LootSelectorLootTable(MinecraftKey minecraftkey, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list1) {
        super(i, j, list, list1);
        this.name = minecraftkey;
    }

    @Override
    public LootEntryType getType() {
        return LootEntries.REFERENCE;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {
        LootTable loottable = loottableinfo.getResolver().getLootTable(this.name);

        loottable.getRandomItemsRaw(loottableinfo, consumer);
    }

    @Override
    public void validate(LootCollector lootcollector) {
        LootDataId<LootTable> lootdataid = new LootDataId<>(LootDataType.TABLE, this.name);

        if (lootcollector.hasVisitedElement(lootdataid)) {
            lootcollector.reportProblem("Table " + this.name + " is recursively called");
        } else {
            super.validate(lootcollector);
            lootcollector.resolver().getElementOptional(lootdataid).ifPresentOrElse((loottable) -> {
                loottable.validate(lootcollector.enterElement("->{" + this.name + "}", lootdataid));
            }, () -> {
                lootcollector.reportProblem("Unknown loot table called " + this.name);
            });
        }
    }

    public static LootSelectorEntry.a<?> lootTableReference(MinecraftKey minecraftkey) {
        return simpleBuilder((i, j, list, list1) -> {
            return new LootSelectorLootTable(minecraftkey, i, j, list, list1);
        });
    }
}
