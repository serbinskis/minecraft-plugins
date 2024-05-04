package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3807 extends DataConverterSchemaNamed {

    public V3807(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        schema.register(map, "minecraft:vault", () -> {
            return DSL.optionalFields("config", DSL.optionalFields("key_item", DataConverterTypes.ITEM_STACK.in(schema)), "server_data", DSL.optionalFields("items_to_eject", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), "shared_data", DSL.optionalFields("display_item", DataConverterTypes.ITEM_STACK.in(schema)));
        });
        return map;
    }
}
