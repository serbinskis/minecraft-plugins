package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class DataConverterPainting extends DataConverterNamedEntity {

    private static final Map<String, String> MAP = (Map) DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
        hashmap.put("donkeykong", "donkey_kong");
        hashmap.put("burningskull", "burning_skull");
        hashmap.put("skullandroses", "skull_and_roses");
    });

    public DataConverterPainting(Schema schema, boolean flag) {
        super(schema, flag, "EntityPaintingMotiveFix", DataConverterTypes.ENTITY, "minecraft:painting");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        Optional<String> optional = dynamic.get("Motive").asString().result();

        if (optional.isPresent()) {
            String s = ((String) optional.get()).toLowerCase(Locale.ROOT);

            return dynamic.set("Motive", dynamic.createString(DataConverterSchemaNamed.ensureNamespaced((String) DataConverterPainting.MAP.getOrDefault(s, s))));
        } else {
            return dynamic;
        }
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}
