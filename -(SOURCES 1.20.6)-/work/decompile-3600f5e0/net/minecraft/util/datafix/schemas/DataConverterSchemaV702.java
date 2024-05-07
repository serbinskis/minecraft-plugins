package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class DataConverterSchemaV702 extends Schema {

    public DataConverterSchemaV702(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);

        schema.register(map, "ZombieVillager", (s) -> {
            return DSL.optionalFields("Offers", DSL.optionalFields("Recipes", DSL.list(DataConverterTypes.VILLAGER_TRADE.in(schema))), DataConverterSchemaV100.equipment(schema));
        });
        schema.register(map, "Husk", () -> {
            return DataConverterSchemaV100.equipment(schema);
        });
        return map;
    }
}
