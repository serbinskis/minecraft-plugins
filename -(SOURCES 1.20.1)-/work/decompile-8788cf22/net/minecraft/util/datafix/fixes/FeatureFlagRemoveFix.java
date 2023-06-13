package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureFlagRemoveFix extends DataFix {

    private final String name;
    private final Set<String> flagsToRemove;

    public FeatureFlagRemoveFix(Schema schema, String s, Set<String> set) {
        super(schema, false);
        this.name = s;
        this.flagsToRemove = set;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(DataConverterTypes.LEVEL), (typed) -> {
            return typed.update(DSL.remainderFinder(), this::fixTag);
        });
    }

    private <T> Dynamic<T> fixTag(Dynamic<T> dynamic) {
        List<Dynamic<T>> list = (List) dynamic.get("removed_features").asStream().collect(Collectors.toCollection(ArrayList::new));
        Dynamic<T> dynamic1 = dynamic.update("enabled_features", (dynamic2) -> {
            Optional optional = dynamic2.asStreamOpt().result().map((stream) -> {
                return stream.filter((dynamic3) -> {
                    Optional<String> optional1 = dynamic3.asString().result();

                    if (optional1.isEmpty()) {
                        return true;
                    } else {
                        boolean flag = this.flagsToRemove.contains(optional1.get());

                        if (flag) {
                            list.add(dynamic.createString((String) optional1.get()));
                        }

                        return !flag;
                    }
                });
            });

            Objects.requireNonNull(dynamic);
            return (Dynamic) DataFixUtils.orElse(optional.map(dynamic::createList), dynamic2);
        });

        if (!list.isEmpty()) {
            dynamic1 = dynamic1.set("removed_features", dynamic.createList(list.stream()));
        }

        return dynamic1;
    }
}
