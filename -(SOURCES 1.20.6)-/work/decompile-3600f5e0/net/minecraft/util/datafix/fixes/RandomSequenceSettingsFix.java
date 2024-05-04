package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class RandomSequenceSettingsFix extends DataFix {

    public RandomSequenceSettingsFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("RandomSequenceSettingsFix", this.getInputSchema().getType(DataConverterTypes.SAVED_DATA_RANDOM_SEQUENCES), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("data", (dynamic1) -> {
                    return dynamic1.emptyMap().set("sequences", dynamic1);
                });
            });
        });
    }
}
