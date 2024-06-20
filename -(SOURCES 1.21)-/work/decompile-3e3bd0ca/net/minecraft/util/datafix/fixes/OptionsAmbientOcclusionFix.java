package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsAmbientOcclusionFix extends DataFix {

    public OptionsAmbientOcclusionFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsAmbientOcclusionFix", this.getInputSchema().getType(DataConverterTypes.OPTIONS), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return (Dynamic) DataFixUtils.orElse(dynamic.get("ao").asString().map((s) -> {
                    return dynamic.set("ao", dynamic.createString(updateValue(s)));
                }).result(), dynamic);
            });
        });
    }

    private static String updateValue(String s) {
        String s1;

        switch (s) {
            case "0":
                s1 = "false";
                break;
            case "1":
            case "2":
                s1 = "true";
                break;
            default:
                s1 = s;
        }

        return s1;
    }
}
