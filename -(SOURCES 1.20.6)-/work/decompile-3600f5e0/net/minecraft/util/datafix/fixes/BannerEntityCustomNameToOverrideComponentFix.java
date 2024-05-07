package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class BannerEntityCustomNameToOverrideComponentFix extends DataFix {

    public BannerEntityCustomNameToOverrideComponentFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY);
        TaggedChoiceType<?> taggedchoicetype = this.getInputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY);
        OpticFinder<?> opticfinder = type.findField("components");

        return this.fixTypeEverywhereTyped("Banner entity custom_name to item_name component fix", type, (typed) -> {
            Object object = ((Pair) typed.get(taggedchoicetype.finder())).getFirst();

            return object.equals("minecraft:banner") ? this.fix(typed, opticfinder) : typed;
        });
    }

    private Typed<?> fix(Typed<?> typed, OpticFinder<?> opticfinder) {
        Dynamic<?> dynamic = (Dynamic) typed.getOptional(DSL.remainderFinder()).orElseThrow();
        OptionalDynamic<?> optionaldynamic = dynamic.get("CustomName");
        boolean flag = optionaldynamic.asString().result().flatMap(ComponentDataFixUtils::extractTranslationString).filter((s) -> {
            return s.equals("block.minecraft.ominous_banner");
        }).isPresent();

        if (flag) {
            Typed<?> typed1 = typed.getOrCreateTyped(opticfinder).update(DSL.remainderFinder(), (dynamic1) -> {
                return dynamic1.set("minecraft:item_name", (Dynamic) optionaldynamic.result().get()).set("minecraft:hide_additional_tooltip", dynamic1.createMap(Map.of()));
            });

            return typed.set(opticfinder, typed1).set(DSL.remainderFinder(), dynamic.remove("CustomName"));
        } else {
            return typed;
        }
    }
}
