package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;

public class LootItemConditions {

    public static final LootItemConditionType INVERTED = register("inverted", LootItemConditionInverted.CODEC);
    public static final LootItemConditionType ANY_OF = register("any_of", AnyOfCondition.CODEC);
    public static final LootItemConditionType ALL_OF = register("all_of", AllOfCondition.CODEC);
    public static final LootItemConditionType RANDOM_CHANCE = register("random_chance", LootItemConditionRandomChance.CODEC);
    public static final LootItemConditionType RANDOM_CHANCE_WITH_ENCHANTED_BONUS = register("random_chance_with_enchanted_bonus", LootItemRandomChanceWithEnchantedBonusCondition.CODEC);
    public static final LootItemConditionType ENTITY_PROPERTIES = register("entity_properties", LootItemConditionEntityProperty.CODEC);
    public static final LootItemConditionType KILLED_BY_PLAYER = register("killed_by_player", LootItemConditionKilledByPlayer.CODEC);
    public static final LootItemConditionType ENTITY_SCORES = register("entity_scores", LootItemConditionEntityScore.CODEC);
    public static final LootItemConditionType BLOCK_STATE_PROPERTY = register("block_state_property", LootItemConditionBlockStateProperty.CODEC);
    public static final LootItemConditionType MATCH_TOOL = register("match_tool", LootItemConditionMatchTool.CODEC);
    public static final LootItemConditionType TABLE_BONUS = register("table_bonus", LootItemConditionTableBonus.CODEC);
    public static final LootItemConditionType SURVIVES_EXPLOSION = register("survives_explosion", LootItemConditionSurvivesExplosion.CODEC);
    public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = register("damage_source_properties", LootItemConditionDamageSourceProperties.CODEC);
    public static final LootItemConditionType LOCATION_CHECK = register("location_check", LootItemConditionLocationCheck.CODEC);
    public static final LootItemConditionType WEATHER_CHECK = register("weather_check", LootItemConditionWeatherCheck.CODEC);
    public static final LootItemConditionType REFERENCE = register("reference", LootItemConditionReference.CODEC);
    public static final LootItemConditionType TIME_CHECK = register("time_check", LootItemConditionTimeCheck.CODEC);
    public static final LootItemConditionType VALUE_CHECK = register("value_check", ValueCheckCondition.CODEC);
    public static final LootItemConditionType ENCHANTMENT_ACTIVE_CHECK = register("enchantment_active_check", EnchantmentActiveCheck.CODEC);

    public LootItemConditions() {}

    private static LootItemConditionType register(String s, MapCodec<? extends LootItemCondition> mapcodec) {
        return (LootItemConditionType) IRegistry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, MinecraftKey.withDefaultNamespace(s), new LootItemConditionType(mapcodec));
    }
}
