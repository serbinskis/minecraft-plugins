package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class StructuresBecomeConfiguredFix extends DataFix {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, StructuresBecomeConfiguredFix.a> CONVERSION_MAP = ImmutableMap.builder().put("mineshaft", StructuresBecomeConfiguredFix.a.biomeMapped(Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft")).put("shipwreck", StructuresBecomeConfiguredFix.a.biomeMapped(Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck")).put("ocean_ruin", StructuresBecomeConfiguredFix.a.biomeMapped(Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"), "minecraft:ocean_ruin_cold")).put("village", StructuresBecomeConfiguredFix.a.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:village_desert", List.of("minecraft:savanna"), "minecraft:village_savanna", List.of("minecraft:snowy_plains"), "minecraft:village_snowy", List.of("minecraft:taiga"), "minecraft:village_taiga"), "minecraft:village_plains")).put("ruined_portal", StructuresBecomeConfiguredFix.a.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:ruined_portal_desert", List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:stony_shore", "minecraft:meadow", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"), "minecraft:ruined_portal_mountain", List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"), "minecraft:ruined_portal_jungle", List.of("minecraft:deep_frozen_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:frozen_ocean", "minecraft:ocean", "minecraft:cold_ocean", "minecraft:lukewarm_ocean", "minecraft:warm_ocean"), "minecraft:ruined_portal_ocean"), "minecraft:ruined_portal")).put("pillager_outpost", StructuresBecomeConfiguredFix.a.trivial("minecraft:pillager_outpost")).put("mansion", StructuresBecomeConfiguredFix.a.trivial("minecraft:mansion")).put("jungle_pyramid", StructuresBecomeConfiguredFix.a.trivial("minecraft:jungle_pyramid")).put("desert_pyramid", StructuresBecomeConfiguredFix.a.trivial("minecraft:desert_pyramid")).put("igloo", StructuresBecomeConfiguredFix.a.trivial("minecraft:igloo")).put("swamp_hut", StructuresBecomeConfiguredFix.a.trivial("minecraft:swamp_hut")).put("stronghold", StructuresBecomeConfiguredFix.a.trivial("minecraft:stronghold")).put("monument", StructuresBecomeConfiguredFix.a.trivial("minecraft:monument")).put("fortress", StructuresBecomeConfiguredFix.a.trivial("minecraft:fortress")).put("endcity", StructuresBecomeConfiguredFix.a.trivial("minecraft:end_city")).put("buried_treasure", StructuresBecomeConfiguredFix.a.trivial("minecraft:buried_treasure")).put("nether_fossil", StructuresBecomeConfiguredFix.a.trivial("minecraft:nether_fossil")).put("bastion_remnant", StructuresBecomeConfiguredFix.a.trivial("minecraft:bastion_remnant")).build();

    public StructuresBecomeConfiguredFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.CHUNK);
        Type<?> type1 = this.getInputSchema().getType(DataConverterTypes.CHUNK);

        return this.writeFixAndRead("StucturesToConfiguredStructures", type, type1, this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.update("structures", (dynamic1) -> {
            return dynamic1.update("starts", (dynamic2) -> {
                return this.updateStarts(dynamic2, dynamic);
            }).update("References", (dynamic2) -> {
                return this.updateReferences(dynamic2, dynamic);
            });
        });
    }

    private Dynamic<?> updateStarts(Dynamic<?> dynamic, Dynamic<?> dynamic1) {
        Map<? extends Dynamic<?>, ? extends Dynamic<?>> map = (Map) dynamic.getMapValues().result().orElse(Map.of());
        HashMap<Dynamic<?>, Dynamic<?>> hashmap = Maps.newHashMap();

        map.forEach((dynamic2, dynamic3) -> {
            if (!dynamic3.get("id").asString("INVALID").equals("INVALID")) {
                Dynamic<?> dynamic4 = this.findUpdatedStructureType(dynamic2, dynamic1);

                if (dynamic4 == null) {
                    StructuresBecomeConfiguredFix.LOGGER.warn("Encountered unknown structure in datafixer: " + dynamic2.asString("<missing key>"));
                } else {
                    hashmap.computeIfAbsent(dynamic4, (dynamic5) -> {
                        return dynamic3.set("id", dynamic4);
                    });
                }
            }
        });
        return dynamic1.createMap(hashmap);
    }

    private Dynamic<?> updateReferences(Dynamic<?> dynamic, Dynamic<?> dynamic1) {
        Map<? extends Dynamic<?>, ? extends Dynamic<?>> map = (Map) dynamic.getMapValues().result().orElse(Map.of());
        HashMap<Dynamic<?>, Dynamic<?>> hashmap = Maps.newHashMap();

        map.forEach((dynamic2, dynamic3) -> {
            if (dynamic3.asLongStream().count() != 0L) {
                Dynamic<?> dynamic4 = this.findUpdatedStructureType(dynamic2, dynamic1);

                if (dynamic4 == null) {
                    StructuresBecomeConfiguredFix.LOGGER.warn("Encountered unknown structure in datafixer: " + dynamic2.asString("<missing key>"));
                } else {
                    hashmap.compute(dynamic4, (dynamic5, dynamic6) -> {
                        return dynamic6 == null ? dynamic3 : dynamic3.createLongList(LongStream.concat(dynamic6.asLongStream(), dynamic3.asLongStream()));
                    });
                }
            }
        });
        return dynamic1.createMap(hashmap);
    }

    @Nullable
    private Dynamic<?> findUpdatedStructureType(Dynamic<?> dynamic, Dynamic<?> dynamic1) {
        String s = dynamic.asString("UNKNOWN").toLowerCase(Locale.ROOT);
        StructuresBecomeConfiguredFix.a structuresbecomeconfiguredfix_a = (StructuresBecomeConfiguredFix.a) StructuresBecomeConfiguredFix.CONVERSION_MAP.get(s);

        if (structuresbecomeconfiguredfix_a == null) {
            return null;
        } else {
            String s1 = structuresbecomeconfiguredfix_a.fallback;

            if (!structuresbecomeconfiguredfix_a.biomeMapping().isEmpty()) {
                Optional<String> optional = this.guessConfiguration(dynamic1, structuresbecomeconfiguredfix_a);

                if (optional.isPresent()) {
                    s1 = (String) optional.get();
                }
            }

            return dynamic1.createString(s1);
        }
    }

    private Optional<String> guessConfiguration(Dynamic<?> dynamic, StructuresBecomeConfiguredFix.a structuresbecomeconfiguredfix_a) {
        Object2IntArrayMap<String> object2intarraymap = new Object2IntArrayMap();

        dynamic.get("sections").asList(Function.identity()).forEach((dynamic1) -> {
            dynamic1.get("biomes").get("palette").asList(Function.identity()).forEach((dynamic2) -> {
                String s = (String) structuresbecomeconfiguredfix_a.biomeMapping().get(dynamic2.asString(""));

                if (s != null) {
                    object2intarraymap.mergeInt(s, 1, Integer::sum);
                }

            });
        });
        return object2intarraymap.object2IntEntrySet().stream().max(Comparator.comparingInt(Entry::getIntValue)).map(java.util.Map.Entry::getKey);
    }

    private static record a(Map<String, String> biomeMapping, String fallback) {

        public static StructuresBecomeConfiguredFix.a trivial(String s) {
            return new StructuresBecomeConfiguredFix.a(Map.of(), s);
        }

        public static StructuresBecomeConfiguredFix.a biomeMapped(Map<List<String>, String> map, String s) {
            return new StructuresBecomeConfiguredFix.a(unpack(map), s);
        }

        private static Map<String, String> unpack(Map<List<String>, String> map) {
            Builder<String, String> builder = ImmutableMap.builder();
            Iterator iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                java.util.Map.Entry<List<String>, String> java_util_map_entry = (java.util.Map.Entry) iterator.next();

                ((List) java_util_map_entry.getKey()).forEach((s) -> {
                    builder.put(s, (String) java_util_map_entry.getValue());
                });
            }

            return builder.build();
        }
    }
}
