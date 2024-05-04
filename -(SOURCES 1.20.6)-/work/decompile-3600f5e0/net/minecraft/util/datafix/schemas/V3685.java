package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3685 extends DataConverterSchemaNamed {

    public V3685(int i, Schema schema) {
        super(i, schema);
    }

    protected static TypeTemplate abstractArrow(Schema schema) {
        return DSL.optionalFields("inBlockState", DataConverterTypes.BLOCK_STATE.in(schema), "item", DataConverterTypes.ITEM_STACK.in(schema));
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);

        schema.register(map, "minecraft:trident", () -> {
            return abstractArrow(schema);
        });
        schema.register(map, "minecraft:spectral_arrow", () -> {
            return abstractArrow(schema);
        });
        schema.register(map, "minecraft:arrow", () -> {
            return abstractArrow(schema);
        });
        return map;
    }
}
