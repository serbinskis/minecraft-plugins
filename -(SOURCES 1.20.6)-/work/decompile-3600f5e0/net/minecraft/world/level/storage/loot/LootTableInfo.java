package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.INamable;
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
    private final HolderGetter.a lootDataResolver;
    private final Set<LootTableInfo.c<?>> visitedElements = Sets.newLinkedHashSet();

    LootTableInfo(LootParams lootparams, RandomSource randomsource, HolderGetter.a holdergetter_a) {
        this.params = lootparams;
        this.random = randomsource;
        this.lootDataResolver = holdergetter_a;
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

    public HolderGetter.a getResolver() {
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

    public static record c<T>(LootDataType<T> type, T value) {

    }

    public static enum EntityTarget implements INamable {

        THIS("this", LootContextParameters.THIS_ENTITY), KILLER("killer", LootContextParameters.KILLER_ENTITY), DIRECT_KILLER("direct_killer", LootContextParameters.DIRECT_KILLER_ENTITY), KILLER_PLAYER("killer_player", LootContextParameters.LAST_DAMAGE_PLAYER);

        public static final INamable.a<LootTableInfo.EntityTarget> CODEC = INamable.fromEnum(LootTableInfo.EntityTarget::values);
        private final String name;
        private final LootContextParameter<? extends Entity> param;

        private EntityTarget(final String s, final LootContextParameter lootcontextparameter) {
            this.name = s;
            this.param = lootcontextparameter;
        }

        public LootContextParameter<? extends Entity> getParam() {
            return this.param;
        }

        public static LootTableInfo.EntityTarget getByName(String s) {
            LootTableInfo.EntityTarget loottableinfo_entitytarget = (LootTableInfo.EntityTarget) LootTableInfo.EntityTarget.CODEC.byName(s);

            if (loottableinfo_entitytarget != null) {
                return loottableinfo_entitytarget;
            } else {
                throw new IllegalArgumentException("Invalid entity target " + s);
            }
        }

        @Override
        public String getSerializedName() {
            return this.name;
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

        public LootTableInfo create(Optional<MinecraftKey> optional) {
            WorldServer worldserver = this.getLevel();
            MinecraftServer minecraftserver = worldserver.getServer();
            Optional optional1 = Optional.ofNullable(this.random).or(() -> {
                Objects.requireNonNull(worldserver);
                return optional.map(worldserver::getRandomSequence);
            });

            Objects.requireNonNull(worldserver);
            RandomSource randomsource = (RandomSource) optional1.orElseGet(worldserver::getRandom);

            return new LootTableInfo(this.params, randomsource, minecraftserver.reloadableRegistries().lookup());
        }
    }
}
