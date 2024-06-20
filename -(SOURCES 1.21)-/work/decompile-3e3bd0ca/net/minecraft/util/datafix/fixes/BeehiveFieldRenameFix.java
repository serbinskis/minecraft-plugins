package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class BeehiveFieldRenameFix extends DataFix {

    public BeehiveFieldRenameFix(Schema schema) {
        super(schema, true);
    }

    private Dynamic<?> fixBeehive(Dynamic<?> dynamic) {
        return dynamic.remove("Bees");
    }

    private Dynamic<?> fixBee(Dynamic<?> dynamic) {
        dynamic = dynamic.remove("EntityData");
        dynamic = dynamic.renameField("TicksInHive", "ticks_in_hive");
        dynamic = dynamic.renameField("MinOccupationTicks", "min_ticks_in_hive");
        return dynamic;
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getChoiceType(DataConverterTypes.BLOCK_ENTITY, "minecraft:beehive");
        OpticFinder<?> opticfinder = DSL.namedChoice("minecraft:beehive", type);
        ListType<?> listtype = (ListType) type.findFieldType("Bees");
        Type<?> type1 = listtype.getElement();
        OpticFinder<?> opticfinder1 = DSL.fieldFinder("Bees", listtype);
        OpticFinder<?> opticfinder2 = DSL.typeFinder(type1);
        Type<?> type2 = this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY);
        Type<?> type3 = this.getOutputSchema().getType(DataConverterTypes.BLOCK_ENTITY);

        return this.fixTypeEverywhereTyped("BeehiveFieldRenameFix", type2, type3, (typed) -> {
            return ExtraDataFixUtils.cast(type3, typed.updateTyped(opticfinder, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), this::fixBeehive).updateTyped(opticfinder1, (typed2) -> {
                    return typed2.updateTyped(opticfinder2, (typed3) -> {
                        return typed3.update(DSL.remainderFinder(), this::fixBee);
                    });
                });
            }));
        });
    }
}
