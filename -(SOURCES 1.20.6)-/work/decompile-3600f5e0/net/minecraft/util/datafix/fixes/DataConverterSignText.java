package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class DataConverterSignText extends DataConverterNamedEntity {

    public DataConverterSignText(Schema schema, boolean flag) {
        super(schema, flag, "BlockEntitySignTextStrictJsonFix", DataConverterTypes.BLOCK_ENTITY, "Sign");
    }

    private Dynamic<?> updateLine(Dynamic<?> dynamic, String s) {
        return dynamic.update(s, ComponentDataFixUtils::rewriteFromLenient);
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            dynamic = this.updateLine(dynamic, "Text1");
            dynamic = this.updateLine(dynamic, "Text2");
            dynamic = this.updateLine(dynamic, "Text3");
            dynamic = this.updateLine(dynamic, "Text4");
            return dynamic;
        });
    }
}
