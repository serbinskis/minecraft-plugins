package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class DataConverterVillagerTrade extends DataFix {

    public DataConverterVillagerTrade(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.VILLAGER_TRADE);
        OpticFinder<?> opticfinder = type.findField("buy");
        OpticFinder<?> opticfinder1 = type.findField("buyB");
        OpticFinder<?> opticfinder2 = type.findField("sell");
        OpticFinder<Pair<String, String>> opticfinder3 = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));
        Function<Typed<?>, Typed<?>> function = (typed) -> {
            return this.updateItemStack(opticfinder3, typed);
        };

        return this.fixTypeEverywhereTyped("Villager trade fix", type, (typed) -> {
            return typed.updateTyped(opticfinder, function).updateTyped(opticfinder1, function).updateTyped(opticfinder2, function);
        });
    }

    private Typed<?> updateItemStack(OpticFinder<Pair<String, String>> opticfinder, Typed<?> typed) {
        return typed.update(opticfinder, (pair) -> {
            return pair.mapSecond((s) -> {
                return Objects.equals(s, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : s;
            });
        });
    }
}
