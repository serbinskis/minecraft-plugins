package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableInfo {

    private final LootParams params;
    private final RandomSource random;
    private final LootDataResolver lootDataResolver;
    private final Set<LootTableInfo.c<?>> visitedElements = Sets.newLinkedHashSet();

    LootTableInfo(LootParams lootparams, RandomSource randomsource, LootDataResolver lootdataresolver) {
        this.params = lootparams;
        this.random = randomsource;
        this.lootDataResolver = lootdataresolver;
    }

    public boolean hasParam(LootContextParameter<?> lootcontextparameter) {
        return this.params.hasParam(lootcontextparameter);
    }

    public <T> T getParam(LootContextParameter<T> lootcontextparameter) {
        return this.params.getParameter(lootcontextparameter);
    }

    public void addDynamicDrops(MinecraftKey minecraftkey, Consumer<ItemStack> consumer) {
        this.params.addDynamicDrops(minecraftkey, consumer);
    }

    @Nullable
    public <T> T getParamOrNull(LootContextParameter<T> lootcontextparameter) {
        return this.params.getParamOrNull(lootcontextparameter);
    }

    public boolean hasVisitedElement(LootTableInfo.c<?> loottableinfo_c) {
        return this.visitedElements.contains(loottableinfo_c);
    }

    public boolean pushVisitedElement(LootTableInfo.c<?> loottableinfo_c) {
        return this.visitedElements.add(loottableinfo_c);
    }

    public void popVisitedElement(LootTableInfo.c<?> loottableinfo_c) {
        this.visitedElements.remove(loottableinfo_c);
    }

    public LootDataResolver getResolver() {
        return this.lootDataResolver;
    }

    public RandomSource getRandom() {
        return this.random;
    }

    public float getLuck() {
        return this.params.getLuck();
    }

    public WorldServer getLevel() {
        return this.params.getLevel();
    }

    public static LootTableInfo.c<LootTable> createVisitedEntry(LootTable loottable) {
        return new LootTableInfo.c<>(LootDataType.TABLE, loottable);
    }

    public static LootTableInfo.c<LootItemCondition> createVisitedEntry(LootItemCondition lootitemcondition) {
        return new LootTableInfo.c<>(LootDataType.PREDICATE, lootitemcondition);
    }

    public static LootTableInfo.c<LootItemFunction> createVisitedEntry(LootItemFunction lootitemfunction) {
        return new LootTableInfo.c<>(LootDataType.MODIFIER, lootitemfunction);
    }

    public static record c<T> (LootDataType<T> type, T value) {

    }

    public static enum EntityTarget {

        THIS("this", LootContextParameters.THIS_ENTITY), KILLER("killer", LootContextParameters.KILLER_ENTITY), DIRECT_KILLER("direct_killer", LootContextParameters.DIRECT_KILLER_ENTITY), KILLER_PLAYER("killer_player", LootContextParameters.LAST_DAMAGE_PLAYER);

        final String name;
        private final LootContextParameter<? extends Entity> param;

        private EntityTarget(String s, LootContextParameter lootcontextparameter) {
            this.name = s;
            this.param = lootcontextparameter;
        }

        public LootContextParameter<? extends Entity> getParam() {
            return this.param;
        }

        public static LootTableInfo.EntityTarget getByName(String s) {
            LootTableInfo.EntityTarget[] aloottableinfo_entitytarget = values();
            int i = aloottableinfo_entitytarget.length;

            for (int j = 0; j < i; ++j) {
                LootTableInfo.EntityTarget loottableinfo_entitytarget = aloottableinfo_entitytarget[j];

                if (loottableinfo_entitytarget.name.equals(s)) {
                    return loottableinfo_entitytarget;
                }
            }

            throw new IllegalArgumentException("Invalid entity target " + s);
        }

        public static class a extends TypeAdapter<LootTableInfo.EntityTarget> {

            public a() {}

            public void write(JsonWriter jsonwriter, LootTableInfo.EntityTarget loottableinfo_entitytarget) throws IOException {
                jsonwriter.value(loottableinfo_entitytarget.name);
            }

            public LootTableInfo.EntityTarget read(JsonReader jsonreader) throws IOException {
                return LootTableInfo.EntityTarget.getByName(jsonreader.nextString());
            }
        }
    }

    public static class Builder {

        private final LootParams params;
        @Nullable
        private RandomSource random;

        public Builder(LootParams lootparams) {
            this.params = lootparams;
        }

        public LootTableInfo.Builder withOptionalRandomSeed(long i) {
            if (i != 0L) {
                this.random = RandomSource.create(i);
            }

            return this;
        }

        public WorldServer getLevel() {
            return this.params.getLevel();
        }

        public LootTableInfo create(@Nullable MinecraftKey minecraftkey) {
            WorldServer worldserver = this.getLevel();
            MinecraftServer minecraftserver = worldserver.getServer();
            RandomSource randomsource;

            if (this.random != null) {
                randomsource = this.random;
            } else if (minecraftkey != null) {
                randomsource = worldserver.getRandomSequence(minecraftkey);
            } else {
                randomsource = worldserver.getRandom();
            }

            return new LootTableInfo(this.params, randomsource, minecraftserver.getLootData());
        }
    }
}
