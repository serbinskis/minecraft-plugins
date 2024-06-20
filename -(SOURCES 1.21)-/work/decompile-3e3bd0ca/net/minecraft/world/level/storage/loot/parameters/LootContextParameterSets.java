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
    public static final LootContextParameterSet ENCHANTED_DAMAGE;
    public static final LootContextParameterSet ENCHANTED_ITEM;
    public static final LootContextParameterSet ENCHANTED_LOCATION;
    public static final LootContextParameterSet ENCHANTED_ENTITY;
    public static final LootContextParameterSet HIT_BLOCK;

    public LootContextParameterSets() {}

    private static LootContextParameterSet register(String s, Consumer<LootContextParameterSet.Builder> consumer) {
        LootContextParameterSet.Builder lootcontextparameterset_builder = new LootContextParameterSet.Builder();

        consumer.accept(lootcontextparameterset_builder);
        LootContextParameterSet lootcontextparameterset = lootcontextparameterset_builder.build();
        MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace(s);
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
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.DAMAGE_SOURCE).optional(LootContextParameters.ATTACKING_ENTITY).optional(LootContextParameters.DIRECT_ATTACKING_ENTITY).optional(LootContextParameters.LAST_DAMAGE_PLAYER);
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
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.LAST_DAMAGE_PLAYER).required(LootContextParameters.DAMAGE_SOURCE).required(LootContextParameters.ATTACKING_ENTITY).required(LootContextParameters.DIRECT_ATTACKING_ENTITY).required(LootContextParameters.ORIGIN).required(LootContextParameters.BLOCK_STATE).required(LootContextParameters.BLOCK_ENTITY).required(LootContextParameters.TOOL).required(LootContextParameters.EXPLOSION_RADIUS);
        });
        BLOCK = register("block", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.BLOCK_STATE).required(LootContextParameters.ORIGIN).required(LootContextParameters.TOOL).optional(LootContextParameters.THIS_ENTITY).optional(LootContextParameters.BLOCK_ENTITY).optional(LootContextParameters.EXPLOSION_RADIUS);
        });
        SHEARING = register("shearing", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.ORIGIN).optional(LootContextParameters.THIS_ENTITY);
        });
        ENCHANTED_DAMAGE = register("enchanted_damage", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ENCHANTMENT_LEVEL).required(LootContextParameters.ORIGIN).required(LootContextParameters.DAMAGE_SOURCE).optional(LootContextParameters.DIRECT_ATTACKING_ENTITY).optional(LootContextParameters.ATTACKING_ENTITY);
        });
        ENCHANTED_ITEM = register("enchanted_item", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.TOOL).required(LootContextParameters.ENCHANTMENT_LEVEL);
        });
        ENCHANTED_LOCATION = register("enchanted_location", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ENCHANTMENT_LEVEL).required(LootContextParameters.ORIGIN).required(LootContextParameters.ENCHANTMENT_ACTIVE);
        });
        ENCHANTED_ENTITY = register("enchanted_entity", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ENCHANTMENT_LEVEL).required(LootContextParameters.ORIGIN);
        });
        HIT_BLOCK = register("hit_block", (lootcontextparameterset_builder) -> {
            lootcontextparameterset_builder.required(LootContextParameters.THIS_ENTITY).required(LootContextParameters.ENCHANTMENT_LEVEL).required(LootContextParameters.ORIGIN).required(LootContextParameters.BLOCK_STATE);
        });
    }
}
