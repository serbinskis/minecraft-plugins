package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.MinecraftKey;

public class LootContextParameterSets {

    private static final BiMap<MinecraftKey, LootContextParameterSet> REGISTRY = HashBiMap.create();
    public static final Codec<LootContextParameterSet> CODEC;
    public static final LootContextParameterSet EMPTY;
    public static final LootContextParameterSet CHEST;
    public static final LootContextParameterSet COMMAND;
    public static final LootContextParameterSet SELECTOR;
    public static final LootContextParameterSet FISHING;
    public static final LootContextParameterSet ENTITY;
    public static final LootContextParameterSet EQUIPMENT;
    public static final LootContextParameterSet ARCHAEOLOGY;
    public static final LootContextParameterSet GIFT;
    public static final LootContextParameterSet PIGLIN_BARTER;
    public static final LootContextParameterSet VAULT;
    public static final LootContextParameterSet ADVANCEMENT_REWARD;
    public static final LootContextParameterSet ADVANCEMENT_ENTITY;
    public static final LootContextParameterSet ADVANCEMENT_LOCATION;
    public static final LootContextParameterSet BLOCK_USE;
    public static final LootContextParameterSet ALL_PARAMS;
    public static final LootContextParameterSet BLOCK;
    public static final LootContextParameterSet SHEARING;

    public LootContextParameterSets() {}

    private static LootContextParameterSet register(String s, Consumer<LootContextParameterSet.Builder> consumer) {
        LootContextParameterSet.Builder lootcontextparameterset_builder = new LootContextParameterSet.Builder();

        consumer.accept(lootcontextparameterset_builder);
        LootContextParameterSet lootcontextparameterset = lootcontextparameterset_builder.build();
        MinecraftKey minecraftkey = new MinecraftKey(s);
        LootContextParameterSet lootcontextparameterset1 = (LootContextParameterSet) LootContextParameterSets.REGISTRY.put(minecraftkey, lootcontextparameterset);

        if (lootcontextparameterset1 != null) {
            throw new IllegalStateException("Loot table parameter set " + String.valueOf(minecraftkey) + " is already registered");
        } else {
            return lootcontextparameterset;
        }
    }

    static {
        Codec codec = MinecraftKey.CODEC;
        Function function = (minecraftkey) -> {
            return (DataResult) Optional.ofNullable((LootContextParameterSet) LootContextParameterSets.REGISTRY.get(minecraftkey)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    return "No parameter set exists with id: '" + String.valueOf(minecraftkey) + "'";
                });
            });
        };
        BiMap bimap = LootContextParameterSets.REGISTRY.inverse();

        Objects.requireNonNull(bimap);
        CODEC = codec.comapFlatMap(function, bimap::get);
        EMPTY = register("empty", (lootcontextparameterset_builder) -> {
        });
        CHEST = register("chest", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY);
        });
        COMMAND = register("command", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY);
        });
        SELECTOR = register("selector", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).required(LootContextParameters.THIS_ENTITY);
        });
        FISHING = register("fishing", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).required(LootContextParameters.TOOL).optional(LootContextParameters.THIS_ENTITY);
        });
        ENTITY = register("entity", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.DAMAGE_SOURCE).optional(LootContextParameters.KILLER_ENTITY).optional(LootContextParameters.DIRECT_KILLER_ENTITY).optional(LootContextParameters.LAST_DAMAGE_PLAYER);
        });
        EQUIPMENT = register("equipment", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).required(LootContextParameters.THIS_ENTITY);
        });
        ARCHAEOLOGY = register("archaeology", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY);
        });
        GIFT = register("gift", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).required(LootContextParameters.THIS_ENTITY);
        });
        PIGLIN_BARTER = register("barter", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY);
        });
        VAULT = register("vault", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY);
        });
        ADVANCEMENT_REWARD = register("advancement_reward", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN);
        });
        ADVANCEMENT_ENTITY = register("advancement_entity", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN);
        });
        ADVANCEMENT_LOCATION = register("advancement_location", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.TOOL).required(LootContextParameters.BLOCK_STATE);
        });
        BLOCK_USE = register("block_use", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.BLOCK_STATE);
        });
        ALL_PARAMS = register("generic", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.LAST_DAMAGE_PLAYER).required(LootContextParameters.DAMAGE_SOURCE).required(LootContextParameters.KILLER_ENTITY).required(LootContextParameters.DIRECT_KILLER_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.BLOCK_STATE).required(LootContextParameters.BLOCK_ENTITY).required(LootContextParameters.TOOL).required(LootContextParameters.EXPLOSION_RADIUS);
        });
        BLOCK = register("block", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.BLOCK_STATE).required(LootContextParameters.ORIGIN).required(LootContextParameters.TOOL).optional(LootContextParameters.THIS_ENTITY).optional(LootContextParameters.BLOCK_ENTITY).optional(LootContextParameters.EXPLOSION_RADIUS);
        });
        SHEARING = register("shearing", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY);
        });
    }
}
