package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class NestedLootTable extends LootSelectorEntry {

    public static final MapCodec<NestedLootTable> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.either(ResourceKey.codec(Registries.LOOT_TABLE), LootTable.DIRECT_CODEC).fieldOf("value").forGetter((nestedloottable) -> {
            return nestedloottable.contents;
        })).and(singletonFields(instance)).apply(instance, NestedLootTable::new);
    });
    private final Either<ResourceKey<LootTable>, LootTable> contents;

    private NestedLootTable(Either<ResourceKey<LootTable>, LootTable> either, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list1) {
        super(i, j, list, list1);
        this.contents = either;
    }

    @Override
    public LootEntryType getType() {
        return LootEntries.LOOT_TABLE;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {
        ((LootTable) this.contents.map((resourcekey) -> {
            return (LootTable) loottableinfo.getResolver().get(Registries.LOOT_TABLE, resourcekey).map(Holder::value).orElse(LootTable.EMPTY);
        }, (loottable) -> {
            return loottable;
        })).getRandomItemsRaw(loottableinfo, consumer);
    }

    @Override
    public void validate(LootCollector lootcollector) {
        Optional<ResourceKey<LootTable>> optional = this.contents.left();

        if (optional.isPresent()) {
            ResourceKey<LootTable> resourcekey = (ResourceKey) optional.get();

            if (lootcollector.hasVisitedElement(resourcekey)) {
                lootcollector.reportProblem("Table " + String.valueOf(resourcekey.location()) + " is recursively called");
                return;
            }
        }

        super.validate(lootcollector);
        this.contents.ifLeft((resourcekey1) -> {
            lootcollector.resolver().get(Registries.LOOT_TABLE, resourcekey1).ifPresentOrElse((holder_c) -> {
                ((LootTable) holder_c.value()).validate(lootcollector.enterElement("->{" + String.valueOf(resourcekey1.location()) + "}", resourcekey1));
            }, () -> {
                lootcollector.reportProblem("Unknown loot table called " + String.valueOf(resourcekey1.location()));
            });
        }).ifRight((loottable) -> {
            loottable.validate(lootcollector.forChild("->{inline}"));
        });
    }

    public static LootSelectorEntry.a<?> lootTableReference(ResourceKey<LootTable> resourcekey) {
        return simpleBuilder((i, j, list, list1) -> {
            return new NestedLootTable(Either.left(resourcekey), i, j, list, list1);
        });
    }

    public static LootSelectorEntry.a<?> inlineLootTable(LootTable loottable) {
        return simpleBuilder((i, j, list, list1) -> {
            return new NestedLootTable(Either.right(loottable), i, j, list, list1);
        });
    }
}
