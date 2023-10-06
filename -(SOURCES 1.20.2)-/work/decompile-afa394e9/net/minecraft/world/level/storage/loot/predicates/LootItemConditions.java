package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;

public class LootItemConditions {

    private static final Codec<LootItemCondition> TYPED_CODEC = BuiltInRegistries.LOOT_CONDITION_TYPE.byNameCodec().dispatch("condition", LootItemCondition::getType, LootItemConditionType::codec);
    public static final Codec<LootItemCondition> CODEC = ExtraCodecs.lazyInitializedCodec(() -> {
        return ExtraCodecs.withAlternative(LootItemConditions.TYPED_CODEC, AllOfCondition.INLINE_CODEC);
    });
    public static final LootItemConditionType INVERTED = register("inverted", LootItemConditionInverted.CODEC);
    public static final LootItemConditionType ANY_OF = register("any_of", AnyOfCondition.CODEC);
    public static final LootItemConditionType ALL_OF = register("all_of", AllOfCondition.CODEC);
    public static final LootItemConditionType RANDOM_CHANCE = register("random_chance", LootItemConditionRandomChance.CODEC);
    public static final LootItemConditionType RANDOM_CHANCE_WITH_LOOTING = register("random_chance_with_looting", LootItemConditionRandomChanceWithLooting.CODEC);
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

    public LootItemConditions() {}

    private static LootItemConditionType register(String s, Codec<? extends LootItemCondition> codec) {
        return (LootItemConditionType) IRegistry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new MinecraftKey(s), new LootItemConditionType(codec));
    }

    public static <T> Predicate<T> andConditions(List<? extends Predicate<T>> list) {
        List<Predicate<T>> list1 = List.copyOf(list);
        Predicate predicate;

        switch (list1.size()) {
            case 0:
                predicate = (object) -> {
                    return true;
                };
                break;
            case 1:
                predicate = (Predicate) list1.get(0);
                break;
            case 2:
                predicate = ((Predicate) list1.get(0)).and((Predicate) list1.get(1));
                break;
            default:
                predicate = (object) -> {
                    Iterator iterator = list1.iterator();

                    Predicate predicate1;

                    do {
                        if (!iterator.hasNext()) {
                            return true;
                        }

                        predicate1 = (Predicate) iterator.next();
                    } while (predicate1.test(object));

                    return false;
                };
        }

        return predicate;
    }

    public static <T> Predicate<T> orConditions(List<? extends Predicate<T>> list) {
        List<Predicate<T>> list1 = List.copyOf(list);
        Predicate predicate;

        switch (list1.size()) {
            case 0:
                predicate = (object) -> {
                    return false;
                };
                break;
            case 1:
                predicate = (Predicate) list1.get(0);
                break;
            case 2:
                predicate = ((Predicate) list1.get(0)).or((Predicate) list1.get(1));
                break;
            default:
                predicate = (object) -> {
                    Iterator iterator = list1.iterator();

                    Predicate predicate1;

                    do {
                        if (!iterator.hasNext()) {
                            return false;
                        }

                        predicate1 = (Predicate) iterator.next();
                    } while (!predicate1.test(object));

                    return true;
                };
        }

        return predicate;
    }
}
