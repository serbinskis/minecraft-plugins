package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class ProjectileStoredWeaponFix extends DataFix {

    public ProjectileStoredWeaponFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ENTITY);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.ENTITY);

        return this.fixTypeEverywhereTyped("Fix Arrow stored weapon", type, type1, ExtraDataFixUtils.chainAllFilters(this.fixChoice("minecraft:arrow"), this.fixChoice("minecraft:spectral_arrow")));
    }

    private Function<Typed<?>, Typed<?>> fixChoice(String s) {
        Type<?> type = this.getInputSchema().getChoiceType(DataConverterTypes.ENTITY, s);
        Type<?> type1 = this.getOutputSchema().getChoiceType(DataConverterTypes.ENTITY, s);

        return fixChoiceCap(s, type, type1);
    }

    private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String s, Type<?> type, Type<T> type1) {
        OpticFinder<?> opticfinder = DSL.namedChoice(s, type);

        return (typed) -> {
            return typed.updateTyped(opticfinder, type1, (typed1) -> {
                return SystemUtils.writeAndReadTypedOrThrow(typed1, type1, UnaryOperator.identity());
            });
        };
    }
}
