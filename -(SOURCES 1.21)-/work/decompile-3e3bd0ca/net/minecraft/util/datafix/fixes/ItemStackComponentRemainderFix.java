package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public abstract class ItemStackComponentRemainderFix extends DataFix {

    private final String name;
    private final String componentId;
    private final String newComponentId;

    public ItemStackComponentRemainderFix(Schema schema, String s, String s1) {
        this(schema, s, s1, s1);
    }

    public ItemStackComponentRemainderFix(Schema schema, String s, String s1, String s2) {
        super(schema, false);
        this.name = s;
        this.componentId = s1;
        this.newComponentId = s2;
    }

    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        OpticFinder<?> opticfinder = type.findField("components");

        return this.fixTypeEverywhereTyped(this.name, type, (typed) -> {
            return typed.updateTyped(opticfinder, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), (dynamic) -> {
                    return dynamic.renameAndFixField(this.componentId, this.newComponentId, this::fixComponent);
                });
            });
        });
    }

    protected abstract <T> Dynamic<T> fixComponent(Dynamic<T> dynamic);
}
