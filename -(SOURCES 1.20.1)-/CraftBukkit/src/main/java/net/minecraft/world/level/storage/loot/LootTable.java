package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionUser;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

// CraftBukkit start
import java.util.stream.Collectors;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.world.LootGenerateEvent;
// CraftBukkit end

public class LootTable {

    static final Logger LOGGER = LogUtils.getLogger();
    public static final LootTable EMPTY = new LootTable(LootContextParameterSets.EMPTY, (MinecraftKey) null, new LootSelector[0], new LootItemFunction[0]);
    public static final LootContextParameterSet DEFAULT_PARAM_SET = LootContextParameterSets.ALL_PARAMS;
    final LootContextParameterSet paramSet;
    @Nullable
    final MinecraftKey randomSequence;
    final LootSelector[] pools;
    final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootTableInfo, ItemStack> compositeFunction;

    LootTable(LootContextParameterSet lootcontextparameterset, @Nullable MinecraftKey minecraftkey, LootSelector[] alootselector, LootItemFunction[] alootitemfunction) {
        this.paramSet = lootcontextparameterset;
        this.randomSequence = minecraftkey;
        this.pools = alootselector;
        this.functions = alootitemfunction;
        this.compositeFunction = LootItemFunctions.compose(alootitemfunction);
    }

    public static Consumer<ItemStack> createStackSplitter(WorldServer worldserver, Consumer<ItemStack> consumer) {
        return (itemstack) -> {
            if (itemstack.isItemEnabled(worldserver.enabledFeatures())) {
                if (itemstack.getCount() < itemstack.getMaxStackSize()) {
                    consumer.accept(itemstack);
                } else {
                    int i = itemstack.getCount();

                    while (i > 0) {
                        ItemStack itemstack1 = itemstack.copyWithCount(Math.min(itemstack.getMaxStackSize(), i));

                        i -= itemstack1.getCount();
                        consumer.accept(itemstack1);
                    }
                }

            }
        };
    }

    public void getRandomItemsRaw(LootParams lootparams, Consumer<ItemStack> consumer) {
        this.getRandomItemsRaw((new LootTableInfo.Builder(lootparams)).create(this.randomSequence), consumer);
    }

    public void getRandomItemsRaw(LootTableInfo loottableinfo, Consumer<ItemStack> consumer) {
        LootTableInfo.c<?> loottableinfo_c = LootTableInfo.createVisitedEntry(this);

        if (loottableinfo.pushVisitedElement(loottableinfo_c)) {
            Consumer<ItemStack> consumer1 = LootItemFunction.decorate(this.compositeFunction, consumer, loottableinfo);
            LootSelector[] alootselector = this.pools;
            int i = alootselector.length;

            for (int j = 0; j < i; ++j) {
                LootSelector lootselector = alootselector[j];

                lootselector.addRandomItems(consumer1, loottableinfo);
            }

            loottableinfo.popVisitedElement(loottableinfo_c);
        } else {
            LootTable.LOGGER.warn("Detected infinite loop in loot tables");
        }

    }

    public void getRandomItems(LootParams lootparams, long i, Consumer<ItemStack> consumer) {
        this.getRandomItemsRaw((new LootTableInfo.Builder(lootparams)).withOptionalRandomSeed(i).create(this.randomSequence), createStackSplitter(lootparams.getLevel(), consumer));
    }

    public void getRandomItems(LootParams lootparams, Consumer<ItemStack> consumer) {
        this.getRandomItemsRaw(lootparams, createStackSplitter(lootparams.getLevel(), consumer));
    }

    public void getRandomItems(LootTableInfo loottableinfo, Consumer<ItemStack> consumer) {
        this.getRandomItemsRaw(loottableinfo, createStackSplitter(loottableinfo.getLevel(), consumer));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams lootparams, long i) {
        return this.getRandomItems((new LootTableInfo.Builder(lootparams)).withOptionalRandomSeed(i).create(this.randomSequence));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams lootparams) {
        return this.getRandomItems((new LootTableInfo.Builder(lootparams)).create(this.randomSequence));
    }

    private ObjectArrayList<ItemStack> getRandomItems(LootTableInfo loottableinfo) {
        ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList();

        Objects.requireNonNull(objectarraylist);
        this.getRandomItems(loottableinfo, objectarraylist::add);
        return objectarraylist;
    }

    public LootContextParameterSet getParamSet() {
        return this.paramSet;
    }

    public void validate(LootCollector lootcollector) {
        int i;

        for (i = 0; i < this.pools.length; ++i) {
            this.pools[i].validate(lootcollector.forChild(".pools[" + i + "]"));
        }

        for (i = 0; i < this.functions.length; ++i) {
            this.functions[i].validate(lootcollector.forChild(".functions[" + i + "]"));
        }

    }

    public void fill(IInventory iinventory, LootParams lootparams, long i) {
        // CraftBukkit start
        this.fillInventory(iinventory, lootparams, i, false);
    }

    public void fillInventory(IInventory iinventory, LootParams lootparams, long i, boolean plugin) {
        // CraftBukkit end
        LootTableInfo loottableinfo = (new LootTableInfo.Builder(lootparams)).withOptionalRandomSeed(i).create(this.randomSequence);
        ObjectArrayList<ItemStack> objectarraylist = this.getRandomItems(loottableinfo);
        RandomSource randomsource = loottableinfo.getRandom();
        // CraftBukkit start
        LootGenerateEvent event = CraftEventFactory.callLootGenerateEvent(iinventory, this, loottableinfo, objectarraylist, plugin);
        if (event.isCancelled()) {
            return;
        }
        objectarraylist = event.getLoot().stream().map(CraftItemStack::asNMSCopy).collect(ObjectArrayList.toList());
        // CraftBukkit end
        List<Integer> list = this.getAvailableSlots(iinventory, randomsource);

        this.shuffleAndSplitItems(objectarraylist, list.size(), randomsource);
        ObjectListIterator objectlistiterator = objectarraylist.iterator();

        while (objectlistiterator.hasNext()) {
            ItemStack itemstack = (ItemStack) objectlistiterator.next();

            if (list.isEmpty()) {
                LootTable.LOGGER.warn("Tried to over-fill a container");
                return;
            }

            if (itemstack.isEmpty()) {
                iinventory.setItem((Integer) list.remove(list.size() - 1), ItemStack.EMPTY);
            } else {
                iinventory.setItem((Integer) list.remove(list.size() - 1), itemstack);
            }
        }

    }

    private void shuffleAndSplitItems(ObjectArrayList<ItemStack> objectarraylist, int i, RandomSource randomsource) {
        List<ItemStack> list = Lists.newArrayList();
        ObjectListIterator objectlistiterator = objectarraylist.iterator();

        while (objectlistiterator.hasNext()) {
            ItemStack itemstack = (ItemStack) objectlistiterator.next();

            if (itemstack.isEmpty()) {
                objectlistiterator.remove();
            } else if (itemstack.getCount() > 1) {
                list.add(itemstack);
                objectlistiterator.remove();
            }
        }

        while (i - objectarraylist.size() - list.size() > 0 && !list.isEmpty()) {
            ItemStack itemstack1 = (ItemStack) list.remove(MathHelper.nextInt(randomsource, 0, list.size() - 1));
            int j = MathHelper.nextInt(randomsource, 1, itemstack1.getCount() / 2);
            ItemStack itemstack2 = itemstack1.split(j);

            if (itemstack1.getCount() > 1 && randomsource.nextBoolean()) {
                list.add(itemstack1);
            } else {
                objectarraylist.add(itemstack1);
            }

            if (itemstack2.getCount() > 1 && randomsource.nextBoolean()) {
                list.add(itemstack2);
            } else {
                objectarraylist.add(itemstack2);
            }
        }

        objectarraylist.addAll(list);
        SystemUtils.shuffle(objectarraylist, randomsource);
    }

    private List<Integer> getAvailableSlots(IInventory iinventory, RandomSource randomsource) {
        ObjectArrayList<Integer> objectarraylist = new ObjectArrayList();

        for (int i = 0; i < iinventory.getContainerSize(); ++i) {
            if (iinventory.getItem(i).isEmpty()) {
                objectarraylist.add(i);
            }
        }

        SystemUtils.shuffle(objectarraylist, randomsource);
        return objectarraylist;
    }

    public static LootTable.a lootTable() {
        return new LootTable.a();
    }

    public static class a implements LootItemFunctionUser<LootTable.a> {

        private final List<LootSelector> pools = Lists.newArrayList();
        private final List<LootItemFunction> functions = Lists.newArrayList();
        private LootContextParameterSet paramSet;
        @Nullable
        private MinecraftKey randomSequence;

        public a() {
            this.paramSet = LootTable.DEFAULT_PARAM_SET;
            this.randomSequence = null;
        }

        public LootTable.a withPool(LootSelector.a lootselector_a) {
            this.pools.add(lootselector_a.build());
            return this;
        }

        public LootTable.a setParamSet(LootContextParameterSet lootcontextparameterset) {
            this.paramSet = lootcontextparameterset;
            return this;
        }

        public LootTable.a setRandomSequence(MinecraftKey minecraftkey) {
            this.randomSequence = minecraftkey;
            return this;
        }

        @Override
        public LootTable.a apply(LootItemFunction.a lootitemfunction_a) {
            this.functions.add(lootitemfunction_a.build());
            return this;
        }

        @Override
        public LootTable.a unwrap() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.paramSet, this.randomSequence, (LootSelector[]) this.pools.toArray(new LootSelector[0]), (LootItemFunction[]) this.functions.toArray(new LootItemFunction[0]));
        }
    }

    public static class b implements JsonDeserializer<LootTable>, JsonSerializer<LootTable> {

        public b() {}

        public LootTable deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            JsonObject jsonobject = ChatDeserializer.convertToJsonObject(jsonelement, "loot table");
            LootSelector[] alootselector = (LootSelector[]) ChatDeserializer.getAsObject(jsonobject, "pools", new LootSelector[0], jsondeserializationcontext, LootSelector[].class);
            LootContextParameterSet lootcontextparameterset = null;

            if (jsonobject.has("type")) {
                String s = ChatDeserializer.getAsString(jsonobject, "type");

                lootcontextparameterset = LootContextParameterSets.get(new MinecraftKey(s));
            }

            MinecraftKey minecraftkey;

            if (jsonobject.has("random_sequence")) {
                String s1 = ChatDeserializer.getAsString(jsonobject, "random_sequence");

                minecraftkey = new MinecraftKey(s1);
            } else {
                minecraftkey = null;
            }

            LootItemFunction[] alootitemfunction = (LootItemFunction[]) ChatDeserializer.getAsObject(jsonobject, "functions", new LootItemFunction[0], jsondeserializationcontext, LootItemFunction[].class);

            return new LootTable(lootcontextparameterset != null ? lootcontextparameterset : LootContextParameterSets.ALL_PARAMS, minecraftkey, alootselector, alootitemfunction);
        }

        public JsonElement serialize(LootTable loottable, Type type, JsonSerializationContext jsonserializationcontext) {
            JsonObject jsonobject = new JsonObject();

            if (loottable.paramSet != LootTable.DEFAULT_PARAM_SET) {
                MinecraftKey minecraftkey = LootContextParameterSets.getKey(loottable.paramSet);

                if (minecraftkey != null) {
                    jsonobject.addProperty("type", minecraftkey.toString());
                } else {
                    LootTable.LOGGER.warn("Failed to find id for param set {}", loottable.paramSet);
                }
            }

            if (loottable.randomSequence != null) {
                jsonobject.addProperty("random_sequence", loottable.randomSequence.toString());
            }

            if (loottable.pools.length > 0) {
                jsonobject.add("pools", jsonserializationcontext.serialize(loottable.pools));
            }

            if (!ArrayUtils.isEmpty(loottable.functions)) {
                jsonobject.add("functions", jsonserializationcontext.serialize(loottable.functions));
            }

            return jsonobject;
        }
    }
}
