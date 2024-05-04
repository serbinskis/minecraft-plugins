package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class DataConverterMapId extends DataFix {

    public DataConverterMapId(Schema schema, boolean flag) {
        super(schema, flag);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("Map id fix", this.getInputSchema().getType(DataConverterTypes.SAVED_DATA_MAP_DATA), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.createMap(ImmutableMap.of(dynamic.createString("data"), dynamic));
            });
        });
    }
}
