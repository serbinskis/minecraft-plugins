package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class DataConverterAttributes extends DataFix {

    private final String name;
    private final UnaryOperator<String> renames;

    public DataConverterAttributes(Schema schema, String s, UnaryOperator<String> unaryoperator) {
        super(schema, false);
        this.name = s;
        this.renames = unaryoperator;
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        OpticFinder<?> opticfinder = type.findField("tag");

        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped(this.name + " (ItemStack)", type, (typed) -> {
            return typed.updateTyped(opticfinder, this::fixItemStackTag);
        }), new TypeRewriteRule[]{this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(DataConverterTypes.ENTITY), this::fixEntity), this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(DataConverterTypes.PLAYER), this::fixEntity)});
    }

    private Dynamic<?> fixName(Dynamic<?> dynamic) {
        Optional optional = dynamic.asString().result().map(this.renames);

        Objects.requireNonNull(dynamic);
        return (Dynamic) DataFixUtils.orElse(optional.map(dynamic::createString), dynamic);
    }

    private Typed<?> fixItemStackTag(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.update("AttributeModifiers", (dynamic1) -> {
                Optional optional = dynamic1.asStreamOpt().result().map((stream) -> {
                    return stream.map((dynamic2) -> {
                        return dynamic2.update("AttributeName", this::fixName);
                    });
                });

                Objects.requireNonNull(dynamic1);
                return (Dynamic) DataFixUtils.orElse(optional.map(dynamic1::createList), dynamic1);
            });
        });
    }

    private Typed<?> fixEntity(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.update("Attributes", (dynamic1) -> {
                Optional optional = dynamic1.asStreamOpt().result().map((stream) -> {
                    return stream.map((dynamic2) -> {
                        return dynamic2.update("Name", this::fixName);
                    });
                });

                Objects.requireNonNull(dynamic1);
                return (Dynamic) DataFixUtils.orElse(optional.map(dynamic1::createList), dynamic1);
            });
        });
    }
}
