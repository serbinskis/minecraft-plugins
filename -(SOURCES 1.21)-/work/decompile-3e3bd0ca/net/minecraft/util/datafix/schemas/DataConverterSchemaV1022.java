package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class DataConverterSchemaV1022 extends Schema {

    public DataConverterSchemaV1022(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(false, DataConverterTypes.RECIPE, () -> {
            return DSL.constType(DataConverterSchemaNamed.namespacedString());
        });
        schema.registerType(false, DataConverterTypes.PLAYER, () -> {
            return DSL.optionalFields(new Pair[]{Pair.of("RootVehicle", DSL.optionalFields("Entity", DataConverterTypes.ENTITY_TREE.in(schema))), Pair.of("Inventory", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("EnderItems", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("ShoulderEntityLeft", DataConverterTypes.ENTITY_TREE.in(schema)), Pair.of("ShoulderEntityRight", DataConverterTypes.ENTITY_TREE.in(schema)), Pair.of("recipeBook", DSL.optionalFields("recipes", DSL.list(DataConverterTypes.RECIPE.in(schema)), "toBeDisplayed", DSL.list(DataConverterTypes.RECIPE.in(schema))))});
        });
        schema.registerType(false, DataConverterTypes.HOTBAR, () -> {
            return DSL.compoundList(DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
    }
}
