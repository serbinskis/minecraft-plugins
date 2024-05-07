package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaV1451_6;
import org.apache.commons.lang3.StringUtils;

public class DataConverterStatistic extends DataFix {

    private static final Set<String> SPECIAL_OBJECTIVE_CRITERIA = Set.of("dummy", "trigger", "deathCount", "playerKillCount", "totalKillCount", "health", "food", "air", "armor", "xp", "level", "killedByTeam.aqua", "killedByTeam.black", "killedByTeam.blue", "killedByTeam.dark_aqua", "killedByTeam.dark_blue", "killedByTeam.dark_gray", "killedByTeam.dark_green", "killedByTeam.dark_purple", "killedByTeam.dark_red", "killedByTeam.gold", "killedByTeam.gray", "killedByTeam.green", "killedByTeam.light_purple", "killedByTeam.red", "killedByTeam.white", "killedByTeam.yellow", "teamkill.aqua", "teamkill.black", "teamkill.blue", "teamkill.dark_aqua", "teamkill.dark_blue", "teamkill.dark_gray", "teamkill.dark_green", "teamkill.dark_purple", "teamkill.dark_red", "teamkill.gold", "teamkill.gray", "teamkill.green", "teamkill.light_purple", "teamkill.red", "teamkill.white", "teamkill.yellow");
    private static final Set<String> SKIP = ImmutableSet.builder().add("stat.craftItem.minecraft.spawn_egg").add("stat.useItem.minecraft.spawn_egg").add("stat.breakItem.minecraft.spawn_egg").add("stat.pickup.minecraft.spawn_egg").add("stat.drop.minecraft.spawn_egg").build();
    private static final Map<String, String> CUSTOM_MAP = ImmutableMap.builder().put("stat.leaveGame", "minecraft:leave_game").put("stat.playOneMinute", "minecraft:play_one_minute").put("stat.timeSinceDeath", "minecraft:time_since_death").put("stat.sneakTime", "minecraft:sneak_time").put("stat.walkOneCm", "minecraft:walk_one_cm").put("stat.crouchOneCm", "minecraft:crouch_one_cm").put("stat.sprintOneCm", "minecraft:sprint_one_cm").put("stat.swimOneCm", "minecraft:swim_one_cm").put("stat.fallOneCm", "minecraft:fall_one_cm").put("stat.climbOneCm", "minecraft:climb_one_cm").put("stat.flyOneCm", "minecraft:fly_one_cm").put("stat.diveOneCm", "minecraft:dive_one_cm").put("stat.minecartOneCm", "minecraft:minecart_one_cm").put("stat.boatOneCm", "minecraft:boat_one_cm").put("stat.pigOneCm", "minecraft:pig_one_cm").put("stat.horseOneCm", "minecraft:horse_one_cm").put("stat.aviateOneCm", "minecraft:aviate_one_cm").put("stat.jump", "minecraft:jump").put("stat.drop", "minecraft:drop").put("stat.damageDealt", "minecraft:damage_dealt").put("stat.damageTaken", "minecraft:damage_taken").put("stat.deaths", "minecraft:deaths").put("stat.mobKills", "minecraft:mob_kills").put("stat.animalsBred", "minecraft:animals_bred").put("stat.playerKills", "minecraft:player_kills").put("stat.fishCaught", "minecraft:fish_caught").put("stat.talkedToVillager", "minecraft:talked_to_villager").put("stat.tradedWithVillager", "minecraft:traded_with_villager").put("stat.cakeSlicesEaten", "minecraft:eat_cake_slice").put("stat.cauldronFilled", "minecraft:fill_cauldron").put("stat.cauldronUsed", "minecraft:use_cauldron").put("stat.armorCleaned", "minecraft:clean_armor").put("stat.bannerCleaned", "minecraft:clean_banner").put("stat.brewingstandInteraction", "minecraft:interact_with_brewingstand").put("stat.beaconInteraction", "minecraft:interact_with_beacon").put("stat.dropperInspected", "minecraft:inspect_dropper").put("stat.hopperInspected", "minecraft:inspect_hopper").put("stat.dispenserInspected", "minecraft:inspect_dispenser").put("stat.noteblockPlayed", "minecraft:play_noteblock").put("stat.noteblockTuned", "minecraft:tune_noteblock").put("stat.flowerPotted", "minecraft:pot_flower").put("stat.trappedChestTriggered", "minecraft:trigger_trapped_chest").put("stat.enderchestOpened", "minecraft:open_enderchest").put("stat.itemEnchanted", "minecraft:enchant_item").put("stat.recordPlayed", "minecraft:play_record").put("stat.furnaceInteraction", "minecraft:interact_with_furnace").put("stat.craftingTableInteraction", "minecraft:interact_with_crafting_table").put("stat.chestOpened", "minecraft:open_chest").put("stat.sleepInBed", "minecraft:sleep_in_bed").put("stat.shulkerBoxOpened", "minecraft:open_shulker_box").build();
    private static final String BLOCK_KEY = "stat.mineBlock";
    private static final String NEW_BLOCK_KEY = "minecraft:mined";
    private static final Map<String, String> ITEM_KEYS = ImmutableMap.builder().put("stat.craftItem", "minecraft:crafted").put("stat.useItem", "minecraft:used").put("stat.breakItem", "minecraft:broken").put("stat.pickup", "minecraft:picked_up").put("stat.drop", "minecraft:dropped").build();
    private static final Map<String, String> ENTITY_KEYS = ImmutableMap.builder().put("stat.entityKilledBy", "minecraft:killed_by").put("stat.killEntity", "minecraft:killed").build();
    private static final Map<String, String> ENTITIES = ImmutableMap.builder().put("Bat", "minecraft:bat").put("Blaze", "minecraft:blaze").put("CaveSpider", "minecraft:cave_spider").put("Chicken", "minecraft:chicken").put("Cow", "minecraft:cow").put("Creeper", "minecraft:creeper").put("Donkey", "minecraft:donkey").put("ElderGuardian", "minecraft:elder_guardian").put("Enderman", "minecraft:enderman").put("Endermite", "minecraft:endermite").put("EvocationIllager", "minecraft:evocation_illager").put("Ghast", "minecraft:ghast").put("Guardian", "minecraft:guardian").put("Horse", "minecraft:horse").put("Husk", "minecraft:husk").put("Llama", "minecraft:llama").put("LavaSlime", "minecraft:magma_cube").put("MushroomCow", "minecraft:mooshroom").put("Mule", "minecraft:mule").put("Ozelot", "minecraft:ocelot").put("Parrot", "minecraft:parrot").put("Pig", "minecraft:pig").put("PolarBear", "minecraft:polar_bear").put("Rabbit", "minecraft:rabbit").put("Sheep", "minecraft:sheep").put("Shulker", "minecraft:shulker").put("Silverfish", "minecraft:silverfish").put("SkeletonHorse", "minecraft:skeleton_horse").put("Skeleton", "minecraft:skeleton").put("Slime", "minecraft:slime").put("Spider", "minecraft:spider").put("Squid", "minecraft:squid").put("Stray", "minecraft:stray").put("Vex", "minecraft:vex").put("Villager", "minecraft:villager").put("VindicationIllager", "minecraft:vindication_illager").put("Witch", "minecraft:witch").put("WitherSkeleton", "minecraft:wither_skeleton").put("Wolf", "minecraft:wolf").put("ZombieHorse", "minecraft:zombie_horse").put("PigZombie", "minecraft:zombie_pigman").put("ZombieVillager", "minecraft:zombie_villager").put("Zombie", "minecraft:zombie").build();
    private static final String NEW_CUSTOM_KEY = "minecraft:custom";

    public DataConverterStatistic(Schema schema, boolean flag) {
        super(schema, flag);
    }

    @Nullable
    private static DataConverterStatistic.a unpackLegacyKey(String s) {
        if (DataConverterStatistic.SKIP.contains(s)) {
            return null;
        } else {
            String s1 = (String) DataConverterStatistic.CUSTOM_MAP.get(s);

            if (s1 != null) {
                return new DataConverterStatistic.a("minecraft:custom", s1);
            } else {
                int i = StringUtils.ordinalIndexOf(s, ".", 2);

                if (i < 0) {
                    return null;
                } else {
                    String s2 = s.substring(0, i);
                    String s3;

                    if ("stat.mineBlock".equals(s2)) {
                        s3 = upgradeBlock(s.substring(i + 1).replace('.', ':'));
                        return new DataConverterStatistic.a("minecraft:mined", s3);
                    } else {
                        s3 = (String) DataConverterStatistic.ITEM_KEYS.get(s2);
                        String s4;
                        String s5;
                        String s6;

                        if (s3 != null) {
                            s4 = s.substring(i + 1).replace('.', ':');
                            s5 = upgradeItem(s4);
                            s6 = s5 == null ? s4 : s5;
                            return new DataConverterStatistic.a(s3, s6);
                        } else {
                            s4 = (String) DataConverterStatistic.ENTITY_KEYS.get(s2);
                            if (s4 != null) {
                                s5 = s.substring(i + 1).replace('.', ':');
                                s6 = (String) DataConverterStatistic.ENTITIES.getOrDefault(s5, s5);
                                return new DataConverterStatistic.a(s4, s6);
                            } else {
                                return null;
                            }
                        }
                    }
                }
            }
        }
    }

    public TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq(this.makeStatFixer(), this.makeObjectiveFixer());
    }

    private TypeRewriteRule makeStatFixer() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.STATS);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.STATS);

        return this.fixTypeEverywhereTyped("StatsCounterFix", type, type1, (typed) -> {
            Dynamic<?> dynamic = (Dynamic) typed.get(DSL.remainderFinder());
            Map<Dynamic<?>, Dynamic<?>> map = Maps.newHashMap();
            Optional<? extends Map<? extends Dynamic<?>, ? extends Dynamic<?>>> optional = dynamic.getMapValues().result();

            if (optional.isPresent()) {
                Iterator iterator = ((Map) optional.get()).entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry<? extends Dynamic<?>, ? extends Dynamic<?>> entry = (Entry) iterator.next();

                    if (((Dynamic) entry.getValue()).asNumber().result().isPresent()) {
                        String s = ((Dynamic) entry.getKey()).asString("");
                        DataConverterStatistic.a dataconverterstatistic_a = unpackLegacyKey(s);

                        if (dataconverterstatistic_a != null) {
                            Dynamic<?> dynamic1 = dynamic.createString(dataconverterstatistic_a.type());
                            Dynamic<?> dynamic2 = (Dynamic) map.computeIfAbsent(dynamic1, (dynamic3) -> {
                                return dynamic.emptyMap();
                            });

                            map.put(dynamic1, dynamic2.set(dataconverterstatistic_a.typeKey(), (Dynamic) entry.getValue()));
                        }
                    }
                }
            }

            return SystemUtils.readTypedOrThrow(type1, dynamic.emptyMap().set("stats", dynamic.createMap(map)));
        });
    }

    private TypeRewriteRule makeObjectiveFixer() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.OBJECTIVE);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.OBJECTIVE);

        return this.fixTypeEverywhereTyped("ObjectiveStatFix", type, type1, (typed) -> {
            Dynamic<?> dynamic = (Dynamic) typed.get(DSL.remainderFinder());
            Dynamic<?> dynamic1 = dynamic.update("CriteriaName", (dynamic2) -> {
                Optional optional = dynamic2.asString().result().map((s) -> {
                    if (DataConverterStatistic.SPECIAL_OBJECTIVE_CRITERIA.contains(s)) {
                        return s;
                    } else {
                        DataConverterStatistic.a dataconverterstatistic_a = unpackLegacyKey(s);

                        if (dataconverterstatistic_a == null) {
                            return "dummy";
                        } else {
                            String s1 = DataConverterSchemaV1451_6.packNamespacedWithDot(dataconverterstatistic_a.type);

                            return s1 + ":" + DataConverterSchemaV1451_6.packNamespacedWithDot(dataconverterstatistic_a.typeKey);
                        }
                    }
                });

                Objects.requireNonNull(dynamic2);
                return (Dynamic) DataFixUtils.orElse(optional.map(dynamic2::createString), dynamic2);
            });

            return SystemUtils.readTypedOrThrow(type1, dynamic1);
        });
    }

    @Nullable
    private static String upgradeItem(String s) {
        return DataConverterFlatten.updateItem(s, 0);
    }

    private static String upgradeBlock(String s) {
        return DataConverterFlattenData.upgradeBlock(s);
    }

    private static record a(String type, String typeKey) {

    }
}
