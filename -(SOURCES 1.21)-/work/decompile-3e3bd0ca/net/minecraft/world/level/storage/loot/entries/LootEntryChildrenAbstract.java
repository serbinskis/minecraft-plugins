package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootEntryChildrenAbstract extends LootEntryAbstract {

    protected final List<LootEntryAbstract> children;
    private final LootEntryChildren composedChildren;

    protected LootEntryChildrenAbstract(List<LootEntryAbstract> list, List<LootItemCondition> list1) {
        super(list1);
        this.children = list;
        this.composedChildren = this.compose(list);
    }

    @Override
    public void validate(LootCollector lootcollector) {
        super.validate(lootcollector);
        if (this.children.isEmpty()) {
            lootcollector.reportProblem("Empty children list");
        }

        for (int i = 0; i < this.children.size(); ++i) {
            ((LootEntryAbstract) this.children.get(i)).validate(lootcollector.forChild(".entry[" + i + "]"));
        }

    }

    protected abstract LootEntryChildren compose(List<? extends LootEntryChildren> list);

    @Override
    public final boolean expand(LootTableInfo loottableinfo, Consumer<LootEntry> consumer) {
        return !this.canRun(loottableinfo) ? false : this.composedChildren.expand(loottableinfo, consumer);
    }

    public static <T extends LootEntryChildrenAbstract> MapCodec<T> createCodec(LootEntryChildrenAbstract.a<T> lootentrychildrenabstract_a) {
        return RecordCodecBuilder.mapCodec((instance) -> {
            P2 p2 = instance.group(LootEntries.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter((lootentrychildrenabstract) -> {
                return lootentrychildrenabstract.children;
            })).and(commonFields(instance).t1());

            Objects.requireNonNull(lootentrychildrenabstract_a);
            return p2.apply(instance, lootentrychildrenabstract_a::create);
        });
    }

    @FunctionalInterface
    public interface a<T extends LootEntryChildrenAbstract> {

        T create(List<LootEntryAbstract> list, List<LootItemCondition> list1);
    }
}
