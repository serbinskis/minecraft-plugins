package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionUser;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootSelectorEntry extends LootEntryAbstract {

    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_QUALITY = 0;
    protected final int weight;
    protected final int quality;
    protected final List<LootItemFunction> functions;
    final BiFunction<ItemStack, LootTableInfo, ItemStack> compositeFunction;
    private final LootEntry entry = new LootSelectorEntry.c() {
        @Override
        public void createItemStack(Consumer<ItemStack> consumer, LootTableInfo loottableinfo) {
            LootSelectorEntry.this.createItemStack(LootItemFunction.decorate(LootSelectorEntry.this.compositeFunction, consumer, loottableinfo), loottableinfo);
        }
    };

    protected LootSelectorEntry(int i, int j, List<LootItemCondition> list, List<LootItemFunction> list1) {
        super(list);
        this.weight = i;
        this.quality = j;
        this.functions = list1;
        this.compositeFunction = LootItemFunctions.compose(list1);
    }

    protected static <T extends LootSelectorEntry> P4<Mu<T>, Integer, Integer, List<LootItemCondition>, List<LootItemFunction>> singletonFields(Instance<T> instance) {
        return instance.group(Codec.INT.optionalFieldOf("weight", 1).forGetter((lootselectorentry) -> {
            return lootselectorentry.weight;
        }), Codec.INT.optionalFieldOf("quality", 0).forGetter((lootselectorentry) -> {
            return lootselectorentry.quality;
        })).and(commonFields(instance).t1()).and(LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter((lootselectorentry) -> {
            return lootselectorentry.functions;
        }));
    }

    @Override
    public void validate(LootCollector lootcollector) {
        super.validate(lootcollector);

        for (int i = 0; i < this.functions.size(); ++i) {
            ((LootItemFunction) this.functions.get(i)).validate(lootcollector.forChild(".functions[" + i + "]"));
        }

    }

    protected abstract void createItemStack(Consumer<ItemStack> consumer, LootTableInfo loottableinfo);

    @Override
    public boolean expand(LootTableInfo loottableinfo, Consumer<LootEntry> consumer) {
        if (this.canRun(loottableinfo)) {
            consumer.accept(this.entry);
            return true;
        } else {
            return false;
        }
    }

    public static LootSelectorEntry.a<?> simpleBuilder(LootSelectorEntry.d lootselectorentry_d) {
        return new LootSelectorEntry.b(lootselectorentry_d);
    }

    private static class b extends LootSelectorEntry.a<LootSelectorEntry.b> {

        private final LootSelectorEntry.d constructor;

        public b(LootSelectorEntry.d lootselectorentry_d) {
            this.constructor = lootselectorentry_d;
        }

        @Override
        protected LootSelectorEntry.b getThis() {
            return this;
        }

        @Override
        public LootEntryAbstract build() {
            return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
        }
    }

    @FunctionalInterface
    protected interface d {

        LootSelectorEntry build(int i, int j, List<LootItemCondition> list, List<LootItemFunction> list1);
    }

    public abstract static class a<T extends LootSelectorEntry.a<T>> extends LootEntryAbstract.a<T> implements LootItemFunctionUser<T> {

        protected int weight = 1;
        protected int quality = 0;
        private final Builder<LootItemFunction> functions = ImmutableList.builder();

        public a() {}

        @Override
        public T apply(LootItemFunction.a lootitemfunction_a) {
            this.functions.add(lootitemfunction_a.build());
            return (LootSelectorEntry.a) this.getThis();
        }

        protected List<LootItemFunction> getFunctions() {
            return this.functions.build();
        }

        public T setWeight(int i) {
            this.weight = i;
            return (LootSelectorEntry.a) this.getThis();
        }

        public T setQuality(int i) {
            this.quality = i;
            return (LootSelectorEntry.a) this.getThis();
        }
    }

    protected abstract class c implements LootEntry {

        protected c() {}

        @Override
        public int getWeight(float f) {
            return Math.max(MathHelper.floor((float) LootSelectorEntry.this.weight + (float) LootSelectorEntry.this.quality * f), 0);
        }
    }
}
