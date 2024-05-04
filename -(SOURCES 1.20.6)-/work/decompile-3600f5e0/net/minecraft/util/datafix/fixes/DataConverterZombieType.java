package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;
import net.minecraft.SystemUtils;

public class DataConverterZombieType extends DataConverterEntityName {

    private final Supplier<Type<?>> zombieVillagerType = Suppliers.memoize(() -> {
        return this.getOutputSchema().getChoiceType(DataConverterTypes.ENTITY, "ZombieVillager");
    });

    public DataConverterZombieType(Schema schema) {
        super("EntityZombieSplitFix", schema, true);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String s, Typed<?> typed) {
        if (!s.equals("Zombie")) {
            return Pair.of(s, typed);
        } else {
            Dynamic<?> dynamic = (Dynamic) typed.getOptional(DSL.remainderFinder()).orElseThrow();
            int i = dynamic.get("ZombieType").asInt(0);
            String s1;
            Typed typed1;

            switch (i) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    s1 = "ZombieVillager";
                    typed1 = this.changeSchemaToZombieVillager(typed, i - 1);
                    break;
                case 6:
                    s1 = "Husk";
                    typed1 = typed;
                    break;
                default:
                    s1 = "Zombie";
                    typed1 = typed;
            }

            return Pair.of(s1, typed1.update(DSL.remainderFinder(), (dynamic1) -> {
                return dynamic1.remove("ZombieType");
            }));
        }
    }

    private Typed<?> changeSchemaToZombieVillager(Typed<?> typed, int i) {
        return SystemUtils.writeAndReadTypedOrThrow(typed, (Type) this.zombieVillagerType.get(), (dynamic) -> {
            return dynamic.set("Profession", dynamic.createInt(i));
        });
    }
}
