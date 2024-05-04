package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootSelectorTag extends LootSelectorEntry {

    public static final MapCodec<LootSelectorTag> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(TagKey.codec(Registries.ITEM).fieldOf("name").forGetter((lootselectortag) -> {
            return lootselectortag.tag;
        }), Codec.BOOL.fieldOf("expand").forGetter((lootselectortag) -> {
            return lootselectortag.expand;
        })).and(singletonFields(instance)).apply(instance, LootSelectorTag::new);
    });
    private final TagKey<Item> tag;
    private final boolean expand;

    private LootSelectorTag(TagKey<Item> tagkey, boolean flag, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list1) {
        super(i, j, list, list1);
        this.tag = tagkey;
        this.expand = flag;
    }

    @Override
    public LootEntryType getType() {
        return LootEntries.TAG;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {
        BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).forEach((holder) -> {
            consumer.accept(new ItemStack(holder));
        });
    }

    private boolean expandTag(LootTableInfo loottableinfo, Consumer<LootEntry> consumer) {
        if (!this.canRun(loottableinfo)) {
            return false;
        } else {
            Iterator iterator = BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).iterator();

            while (iterator.hasNext()) {
                final Holder<Item> holder = (Holder) iterator.next();

                consumer.accept(new LootSelectorEntry.c(this) {
                    @Override
                    public void createItemStack(Consumer<ItemStack> consumer1, LootTableInfo loottableinfo1) {
                        consumer1.accept(new ItemStack(holder));
                    }
                });
            }

            return true;
        }
    }

    @Override
    public boolean expand(LootTableInfo loottableinfo, Consumer<LootEntry> consumer) {
        return this.expand ? this.expandTag(loottableinfo, consumer) : super.expand(loottableinfo, consumer);
    }

    public static LootSelectorEntry.a<?> tagContents(TagKey<Item> tagkey) {
        return simpleBuilder((i, j, list, list1) -> {
            return new LootSelectorTag(tagkey, false, i, j, list, list1);
        });
    }

    public static LootSelectorEntry.a<?> expandTag(TagKey<Item> tagkey) {
        return simpleBuilder((i, j, list, list1) -> {
            return new LootSelectorTag(tagkey, true, i, j, list, list1);
        });
    }
}
