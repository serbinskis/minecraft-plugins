package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.SystemUtils;

public class DataConverterMinecart extends DataConverterEntityName {

    public DataConverterMinecart(Schema schema) {
        super("EntityMinecartIdentifiersFix", schema, true);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String s, Typed<?> typed) {
        if (!s.equals("Minecart")) {
            return Pair.of(s, typed);
        } else {
            int i = ((Dynamic) typed.getOrCreate(DSL.remainderFinder())).get("Type").asInt(0);
            String s1;

            switch (i) {
                case 1:
                    s1 = "MinecartChest";
                    break;
                case 2:
                    s1 = "MinecartFurnace";
                    break;
                default:
                    s1 = "MinecartRideable";
            }

            String s2 = s1;
            Type<?> type = (Type) this.getOutputSchema().findChoiceType(DataConverterTypes.ENTITY).types().get(s2);

            return Pair.of(s2, SystemUtils.writeAndReadTypedOrThrow(typed, type, (dynamic) -> {
                return dynamic.remove("Type");
            }));
        }
    }
}
