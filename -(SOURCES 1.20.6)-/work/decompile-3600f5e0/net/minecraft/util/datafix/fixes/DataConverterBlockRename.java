package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public abstract class DataConverterBlockRename extends DataFix {

    private final String name;

    public DataConverterBlockRename(Schema schema, String s) {
        super(schema, false);
        this.name = s;
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.BLOCK_NAME);
        Type<Pair<String, String>> type1 = DSL.named(DataConverterTypes.BLOCK_NAME.typeName(), DataConverterSchemaNamed.namespacedString());

        if (!Objects.equals(type, type1)) {
            throw new IllegalStateException("block type is not what was expected.");
        } else {
            TypeRewriteRule typerewriterule = this.fixTypeEverywhere(this.name + " for block", type1, (dynamicops) -> {
                return (pair) -> {
                    return pair.mapSecond(this::renameBlock);
                };
            });
            TypeRewriteRule typerewriterule1 = this.fixTypeEverywhereTyped(this.name + " for block_state", this.getInputSchema().getType(DataConverterTypes.BLOCK_STATE), (typed) -> {
                return typed.update(DSL.remainderFinder(), this::fixBlockState);
            });
            TypeRewriteRule typerewriterule2 = this.fixTypeEverywhereTyped(this.name + " for flat_block_state", this.getInputSchema().getType(DataConverterTypes.FLAT_BLOCK_STATE), (typed) -> {
                return typed.update(DSL.remainderFinder(), (dynamic) -> {
                    Optional optional = dynamic.asString().result().map(this::fixFlatBlockState);

                    Objects.requireNonNull(dynamic);
                    return (Dynamic) DataFixUtils.orElse(optional.map(dynamic::createString), dynamic);
                });
            });

            return TypeRewriteRule.seq(typerewriterule, new TypeRewriteRule[]{typerewriterule1, typerewriterule2});
        }
    }

    private Dynamic<?> fixBlockState(Dynamic<?> dynamic) {
        Optional<String> optional = dynamic.get("Name").asString().result();

        return optional.isPresent() ? dynamic.set("Name", dynamic.createString(this.renameBlock((String) optional.get()))) : dynamic;
    }

    private String fixFlatBlockState(String s) {
        int i = s.indexOf(91);
        int j = s.indexOf(123);
        int k = s.length();

        if (i > 0) {
            k = i;
        }

        if (j > 0) {
            k = Math.min(k, j);
        }

        String s1 = s.substring(0, k);
        String s2 = this.renameBlock(s1);

        return s2 + s.substring(k);
    }

    protected abstract String renameBlock(String s);

    public static DataFix create(Schema schema, String s, final Function<String, String> function) {
        return new DataConverterBlockRename(schema, s) {
            @Override
            protected String renameBlock(String s1) {
                return (String) function.apply(s1);
            }
        };
    }
}
