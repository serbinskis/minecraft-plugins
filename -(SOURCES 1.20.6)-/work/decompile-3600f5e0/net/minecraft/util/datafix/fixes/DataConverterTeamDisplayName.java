package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class DataConverterTeamDisplayName extends DataFix {

    public DataConverterTeamDisplayName(Schema schema, boolean flag) {
        super(schema, flag);
    }

    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> type = DSL.named(DataConverterTypes.TEAM.typeName(), DSL.remainderType());

        if (!Objects.equals(type, this.getInputSchema().getType(DataConverterTypes.TEAM))) {
            throw new IllegalStateException("Team type is not what was expected.");
        } else {
            return this.fixTypeEverywhere("TeamDisplayNameFix", type, (dynamicops) -> {
                return (pair) -> {
                    return pair.mapSecond((dynamic) -> {
                        return dynamic.update("DisplayName", ComponentDataFixUtils::wrapLiteralStringAsComponent);
                    });
                };
            });
        }
    }
}
