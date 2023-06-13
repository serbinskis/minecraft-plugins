package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.storage.loot.JsonRegistry;
import net.minecraft.world.level.storage.loot.LootSerializer;

public class LootItemConditions {

    public static final LootItemConditionType INVERTED = register("inverted", new LootItemConditionInverted.a());
    public static final LootItemConditionType ANY_OF = register("any_of", new AnyOfCondition.b());
    public static final LootItemConditionType ALL_OF = register("all_of", new AllOfCondition.b());
    public static final LootItemConditionType RANDOM_CHANCE = register("random_chance", new LootItemConditionRandomChance.a());
    public static final LootItemConditionType RANDOM_CHANCE_WITH_LOOTING = register("random_chance_with_looting", new LootItemConditionRandomChanceWithLooting.a());
    public static final LootItemConditionType ENTITY_PROPERTIES = register("entity_properties", new LootItemConditionEntityProperty.a());
    public static final LootItemConditionType KILLED_BY_PLAYER = register("killed_by_player", new LootItemConditionKilledByPlayer.a());
    public static final LootItemConditionType ENTITY_SCORES = register("entity_scores", new LootItemConditionEntityScore.b());
    public static final LootItemConditionType BLOCK_STATE_PROPERTY = register("block_state_property", new LootItemConditionBlockStateProperty.b());
    public static final LootItemConditionType MATCH_TOOL = register("match_tool", new LootItemConditionMatchTool.a());
    public static final LootItemConditionType TABLE_BONUS = register("table_bonus", new LootItemConditionTableBonus.a());
    public static final LootItemConditionType SURVIVES_EXPLOSION = register("survives_explosion", new LootItemConditionSurvivesExplosion.a());
    public static final LootItemConditionType DAMAGE_SOURCE_PROPERTIES = register("damage_source_properties", new LootItemConditionDamageSourceProperties.a());
    public static final LootItemConditionType LOCATION_CHECK = register("location_check", new LootItemConditionLocationCheck.a());
    public static final LootItemConditionType WEATHER_CHECK = register("weather_check", new LootItemConditionWeatherCheck.b());
    public static final LootItemConditionType REFERENCE = register("reference", new LootItemConditionReference.a());
    public static final LootItemConditionType TIME_CHECK = register("time_check", new LootItemConditionTimeCheck.b());
    public static final LootItemConditionType VALUE_CHECK = register("value_check", new ValueCheckCondition.a());

    public LootItemConditions() {}

    private static LootItemConditionType register(String s, LootSerializer<? extends LootItemCondition> lootserializer) {
        return (LootItemConditionType) IRegistry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new MinecraftKey(s), new LootItemConditionType(lootserializer));
    }

    public static Object createGsonAdapter() {
        return JsonRegistry.builder(BuiltInRegistries.LOOT_CONDITION_TYPE, "condition", "condition", LootItemCondition::getType).build();
    }

    public static <T> Predicate<T> andConditions(Predicate<T>[] apredicate) {
        Predicate predicate;

        switch (apredicate.length) {
            case 0:
                predicate = (object) -> {
                    return true;
                };
                break;
            case 1:
                predicate = apredicate[0];
                break;
            case 2:
                predicate = apredicate[0].and(apredicate[1]);
                break;
            default:
                predicate = (object) -> {
                    Predicate[] apredicate1 = apredicate;
                    int i = apredicate.length;

                    for (int j = 0; j < i; ++j) {
                        Predicate<T> predicate1 = apredicate1[j];

                        if (!predicate1.test(object)) {
                            return false;
                        }
                    }

                    return true;
                };
        }

        return predicate;
    }

    public static <T> Predicate<T> orConditions(Predicate<T>[] apredicate) {
        Predicate predicate;

        switch (apredicate.length) {
            case 0:
                predicate = (object) -> {
                    return false;
                };
                break;
            case 1:
                predicate = apredicate[0];
                break;
            case 2:
                predicate = apredicate[0].or(apredicate[1]);
                break;
            default:
                predicate = (object) -> {
                    Predicate[] apredicate1 = apredicate;
                    int i = apredicate.length;

                    for (int j = 0; j < i; ++j) {
                        Predicate<T> predicate1 = apredicate1[j];

                        if (predicate1.test(object)) {
                            return true;
                        }
                    }

                    return false;
                };
        }

        return predicate;
    }
}
