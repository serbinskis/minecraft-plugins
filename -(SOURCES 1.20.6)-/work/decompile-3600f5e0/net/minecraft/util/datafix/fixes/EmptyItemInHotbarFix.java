package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class EmptyItemInHotbarFix extends DataFix {

    public EmptyItemInHotbarFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> opticfinder = DSL.typeFinder(this.getInputSchema().getType(DataConverterTypes.ITEM_STACK));

        return this.fixTypeEverywhereTyped("EmptyItemInHotbarFix", this.getInputSchema().getType(DataConverterTypes.HOTBAR), (typed) -> {
            return typed.update(opticfinder, (pair) -> {
                return pair.mapSecond((pair1) -> {
                    Optional<String> optional = ((Either) pair1.getFirst()).left().map(Pair::getSecond);
                    Dynamic<?> dynamic = (Dynamic) ((Pair) pair1.getSecond()).getSecond();
                    boolean flag = optional.isEmpty() || ((String) optional.get()).equals("minecraft:air");
                    boolean flag1 = dynamic.get("Count").asInt(0) <= 0;

                    return !flag && !flag1 ? pair1 : Pair.of(Either.right(Unit.INSTANCE), Pair.of(Either.right(Unit.INSTANCE), dynamic.emptyMap()));
                });
            });
        });
    }
}
