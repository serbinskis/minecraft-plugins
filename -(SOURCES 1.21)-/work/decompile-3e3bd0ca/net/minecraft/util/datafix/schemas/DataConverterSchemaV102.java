package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class DataConverterSchemaV102 extends Schema {

    public DataConverterSchemaV102(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.ITEM_STACK, () -> {
            return DSL.hook(DSL.optionalFields("id", DataConverterTypes.ITEM_NAME.in(schema), "tag", DSL.optionalFields(new Pair[]{Pair.of("EntityTag", DataConverterTypes.ENTITY_TREE.in(schema)), Pair.of("BlockEntityTag", DataConverterTypes.BLOCK_ENTITY.in(schema)), Pair.of("CanDestroy", DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))), Pair.of("CanPlaceOn", DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))), Pair.of("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("ChargedProjectiles", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)))})), DataConverterSchemaV99.ADD_NAMES, HookFunction.IDENTITY);
        });
    }
}
