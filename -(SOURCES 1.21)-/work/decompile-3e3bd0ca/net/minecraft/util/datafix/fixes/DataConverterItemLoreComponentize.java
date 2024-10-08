package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class DataConverterItemLoreComponentize extends DataFix {

    public DataConverterItemLoreComponentize(Schema schema, boolean flag) {
        super(schema, flag);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        OpticFinder<?> opticfinder = type.findField("tag");

        return this.fixTypeEverywhereTyped("Item Lore componentize", type, (typed) -> {
            return typed.updateTyped(opticfinder, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), (dynamic) -> {
                    return dynamic.update("display", (dynamic1) -> {
                        return dynamic1.update("Lore", (dynamic2) -> {
                            DataResult dataresult = dynamic2.asStreamOpt().map(DataConverterItemLoreComponentize::fixLoreList);

                            Objects.requireNonNull(dynamic2);
                            return (Dynamic) DataFixUtils.orElse(dataresult.map(dynamic2::createList).result(), dynamic2);
                        });
                    });
                });
            });
        });
    }

    private static <T> Stream<Dynamic<T>> fixLoreList(Stream<Dynamic<T>> stream) {
        return stream.map(ComponentDataFixUtils::wrapLiteralStringAsComponent);
    }
}
