package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class LevelLegacyWorldGenSettingsFix extends DataFix {

    private static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
    private static final List<String> OLD_SETTINGS_KEYS = List.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");

    public LevelLegacyWorldGenSettingsFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelLegacyWorldGenSettingsFix", this.getInputSchema().getType(DataConverterTypes.LEVEL), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                Dynamic<?> dynamic1 = dynamic.get("WorldGenSettings").orElseEmptyMap();
                Iterator iterator = LevelLegacyWorldGenSettingsFix.OLD_SETTINGS_KEYS.iterator();

                while (iterator.hasNext()) {
                    String s = (String) iterator.next();
                    Optional<? extends Dynamic<?>> optional = dynamic.get(s).result();

                    if (optional.isPresent()) {
                        dynamic = dynamic.remove(s);
                        dynamic1 = dynamic1.set(s, (Dynamic) optional.get());
                    }
                }

                return dynamic.set("WorldGenSettings", dynamic1);
            });
        });
    }
}
