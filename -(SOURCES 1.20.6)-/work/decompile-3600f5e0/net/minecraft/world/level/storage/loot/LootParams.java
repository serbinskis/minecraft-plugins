package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;

public class LootParams {

    private final WorldServer level;
    private final Map<LootContextParameter<?>, Object> params;
    private final Map<MinecraftKey, LootParams.b> dynamicDrops;
    private final float luck;

    public LootParams(WorldServer worldserver, Map<LootContextParameter<?>, Object> map, Map<MinecraftKey, LootParams.b> map1, float f) {
        this.level = worldserver;
        this.params = map;
        this.dynamicDrops = map1;
        this.luck = f;
    }

    public WorldServer getLevel() {
        return this.level;
    }

    public boolean hasParam(LootContextParameter<?> lootcontextparameter) {
        return this.params.containsKey(lootcontextparameter);
    }

    public <T> T getParameter(LootContextParameter<T> lootcontextparameter) {
        T t0 = this.params.get(lootcontextparameter);

        if (t0 == null) {
            throw new NoSuchElementException(lootcontextparameter.getName().toString());
        } else {
            return t0;
        }
    }

    @Nullable
    public <T> T getOptionalParameter(LootContextParameter<T> lootcontextparameter) {
        return this.params.get(lootcontextparameter);
    }

    @Nullable
    public <T> T getParamOrNull(LootContextParameter<T> lootcontextparameter) {
        return this.params.get(lootcontextparameter);
    }

    public void addDynamicDrops(MinecraftKey minecraftkey, Consumer<ItemStack> consumer) {
        LootParams.b lootparams_b = (LootParams.b) this.dynamicDrops.get(minecraftkey);

        if (lootparams_b != null) {
            lootparams_b.add(consumer);
        }

    }

    public float getLuck() {
        return this.luck;
    }

    @FunctionalInterface
    public interface b {

        void add(Consumer<ItemStack> consumer);
    }

    public static class a {

        private final WorldServer level;
        private final Map<LootContextParameter<?>, Object> params = Maps.newIdentityHashMap();
        private final Map<MinecraftKey, LootParams.b> dynamicDrops = Maps.newHashMap();
        private float luck;

        public a(WorldServer worldserver) {
            this.level = worldserver;
        }

        public WorldServer getLevel() {
            return this.level;
        }

        public <T> LootParams.a withParameter(LootContextParameter<T> lootcontextparameter, T t0) {
            this.params.put(lootcontextparameter, t0);
            return this;
        }

        public <T> LootParams.a withOptionalParameter(LootContextParameter<T> lootcontextparameter, @Nullable T t0) {
            if (t0 == null) {
                this.params.remove(lootcontextparameter);
            } else {
                this.params.put(lootcontextparameter, t0);
            }

            return this;
        }

        public <T> T getParameter(LootContextParameter<T> lootcontextparameter) {
            T t0 = this.params.get(lootcontextparameter);

            if (t0 == null) {
                throw new NoSuchElementException(lootcontextparameter.getName().toString());
            } else {
                return t0;
            }
        }

        @Nullable
        public <T> T getOptionalParameter(LootContextParameter<T> lootcontextparameter) {
            return this.params.get(lootcontextparameter);
        }

        public LootParams.a withDynamicDrop(MinecraftKey minecraftkey, LootParams.b lootparams_b) {
            LootParams.b lootparams_b1 = (LootParams.b) this.dynamicDrops.put(minecraftkey, lootparams_b);

            if (lootparams_b1 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + String.valueOf(this.dynamicDrops) + "'");
            } else {
                return this;
            }
        }

        public LootParams.a withLuck(float f) {
            this.luck = f;
            return this;
        }

        public LootParams create(LootContextParameterSet lootcontextparameterset) {
            Set<LootContextParameter<?>> set = Sets.difference(this.params.keySet(), lootcontextparameterset.getAllowed());

            if (!set.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + String.valueOf(set));
            } else {
                Set<LootContextParameter<?>> set1 = Sets.difference(lootcontextparameterset.getRequired(), this.params.keySet());

                if (!set1.isEmpty()) {
                    throw new IllegalArgumentException("Missing required parameters: " + String.valueOf(set1));
                } else {
                    return new LootParams(this.level, this.params, this.dynamicDrops, this.luck);
                }
            }
        }
    }
}
