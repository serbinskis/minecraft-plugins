package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class AreaEffectCloudPotionFix extends DataConverterNamedEntity {

    public AreaEffectCloudPotionFix(Schema schema) {
        super(schema, false, "AreaEffectCloudPotionFix", DataConverterTypes.ENTITY, "minecraft:area_effect_cloud");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fix);
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional<Dynamic<T>> optional = dynamic.get("Color").result();
        Optional<Dynamic<T>> optional1 = dynamic.get("effects").result();
        Optional<Dynamic<T>> optional2 = dynamic.get("Potion").result();

        dynamic = dynamic.remove("Color").remove("effects").remove("Potion");
        if (optional.isEmpty() && optional1.isEmpty() && optional2.isEmpty()) {
            return dynamic;
        } else {
            Dynamic<T> dynamic1 = dynamic.emptyMap();

            if (optional.isPresent()) {
                dynamic1 = dynamic1.set("custom_color", (Dynamic) optional.get());
            }

            if (optional1.isPresent()) {
                dynamic1 = dynamic1.set("custom_effects", (Dynamic) optional1.get());
            }

            if (optional2.isPresent()) {
                dynamic1 = dynamic1.set("potion", (Dynamic) optional2.get());
            }

            return dynamic.set("potion_contents", dynamic1);
        }
    }
}
