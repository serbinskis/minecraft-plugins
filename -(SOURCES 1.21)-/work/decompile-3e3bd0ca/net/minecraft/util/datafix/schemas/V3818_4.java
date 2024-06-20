package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3818_4 extends DataConverterSchemaNamed {

    public V3818_4(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.PARTICLE, () -> {
            return DSL.optionalFields("item", DataConverterTypes.ITEM_STACK.in(schema), "block_state", DataConverterTypes.BLOCK_STATE.in(schema));
        });
    }
}
