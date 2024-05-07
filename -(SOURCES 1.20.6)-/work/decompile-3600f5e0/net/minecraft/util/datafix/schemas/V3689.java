package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3689 extends DataConverterSchemaNamed {

    public V3689(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);

        schema.register(map, "minecraft:breeze", () -> {
            return DataConverterSchemaV100.equipment(schema);
        });
        schema.registerSimple(map, "minecraft:wind_charge");
        schema.registerSimple(map, "minecraft:breeze_wind_charge");
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        schema.register(map, "minecraft:trial_spawner", () -> {
            return DSL.optionalFields("spawn_potentials", DSL.list(DSL.fields("data", DSL.fields("entity", DataConverterTypes.ENTITY_TREE.in(schema)))), "spawn_data", DSL.fields("entity", DataConverterTypes.ENTITY_TREE.in(schema)));
        });
        return map;
    }
}
