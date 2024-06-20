package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;

public class ChestedHorsesInventoryZeroIndexingFix extends DataFix {

    public ChestedHorsesInventoryZeroIndexingFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticfinder = DSL.typeFinder(this.getInputSchema().getType(DataConverterTypes.ITEM_STACK));
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ENTITY);

        return TypeRewriteRule.seq(this.horseLikeInventoryIndexingFixer(opticfinder, type, "minecraft:llama"), new TypeRewriteRule[]{this.horseLikeInventoryIndexingFixer(opticfinder, type, "minecraft:trader_llama"), this.horseLikeInventoryIndexingFixer(opticfinder, type, "minecraft:mule"), this.horseLikeInventoryIndexingFixer(opticfinder, type, "minecraft:donkey")});
    }

    private TypeRewriteRule horseLikeInventoryIndexingFixer(OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticfinder, Type<?> type, String s) {
        Type<?> type1 = this.getInputSchema().getChoiceType(DataConverterTypes.ENTITY, s);
        OpticFinder<?> opticfinder1 = DSL.namedChoice(s, type1);
        OpticFinder<?> opticfinder2 = type1.findField("Items");

        return this.fixTypeEverywhereTyped("Fix non-zero indexing in chest horse type " + s, type, (typed) -> {
            return typed.updateTyped(opticfinder1, (typed1) -> {
                return typed1.updateTyped(opticfinder2, (typed2) -> {
                    return typed2.update(opticfinder, (pair) -> {
                        return pair.mapSecond((pair1) -> {
                            return pair1.mapSecond((pair2) -> {
                                return pair2.mapSecond((dynamic) -> {
                                    return dynamic.update("Slot", (dynamic1) -> {
                                        return dynamic1.createByte((byte) (dynamic1.asInt(2) - 2));
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }
}
