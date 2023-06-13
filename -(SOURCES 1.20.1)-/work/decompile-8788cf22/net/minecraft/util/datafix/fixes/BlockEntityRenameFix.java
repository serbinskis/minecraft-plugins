package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.function.UnaryOperator;

public class BlockEntityRenameFix extends DataFix {

    private final String name;
    private final UnaryOperator<String> nameChangeLookup;

    private BlockEntityRenameFix(Schema schema, String s, UnaryOperator<String> unaryoperator) {
        super(schema, true);
        this.name = s;
        this.nameChangeLookup = unaryoperator;
    }

    public TypeRewriteRule makeRule() {
        TaggedChoiceType<String> taggedchoicetype = this.getInputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY);
        TaggedChoiceType<String> taggedchoicetype1 = this.getOutputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY);

        return this.fixTypeEverywhere(this.name, taggedchoicetype, taggedchoicetype1, (dynamicops) -> {
            return (pair) -> {
                return pair.mapFirst(this.nameChangeLookup);
            };
        });
    }

    public static DataFix create(Schema schema, String s, UnaryOperator<String> unaryoperator) {
        return new BlockEntityRenameFix(schema, s, unaryoperator);
    }
}
