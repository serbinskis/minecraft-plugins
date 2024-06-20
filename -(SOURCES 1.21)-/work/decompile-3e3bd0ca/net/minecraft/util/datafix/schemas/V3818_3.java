package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3818_3 extends DataConverterSchemaNamed {

    public V3818_3(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.DATA_COMPONENTS, () -> {
            return DSL.optionalFields(new Pair[]{Pair.of("minecraft:bees", DSL.list(DSL.optionalFields("entity_data", DataConverterTypes.ENTITY_TREE.in(schema)))), Pair.of("minecraft:block_entity_data", DataConverterTypes.BLOCK_ENTITY.in(schema)), Pair.of("minecraft:bundle_contents", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("minecraft:can_break", DSL.optionalFields("predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(DataConverterTypes.BLOCK_NAME.in(schema), DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))))))), Pair.of("minecraft:can_place_on", DSL.optionalFields("predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(DataConverterTypes.BLOCK_NAME.in(schema), DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))))))), Pair.of("minecraft:charged_projectiles", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("minecraft:container", DSL.list(DSL.optionalFields("item", DataConverterTypes.ITEM_STACK.in(schema)))), Pair.of("minecraft:entity_data", DataConverterTypes.ENTITY_TREE.in(schema)), Pair.of("minecraft:pot_decorations", DSL.list(DataConverterTypes.ITEM_NAME.in(schema))), Pair.of("minecraft:food", DSL.optionalFields("using_converts_to", DataConverterTypes.ITEM_STACK.in(schema)))});
        });
    }
}
