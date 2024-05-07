package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class PlayerHeadBlockProfileFix extends DataConverterNamedEntity {

    public PlayerHeadBlockProfileFix(Schema schema) {
        super(schema, false, "PlayerHeadBlockProfileFix", DataConverterTypes.BLOCK_ENTITY, "minecraft:skull");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fix);
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional<Dynamic<T>> optional = dynamic.get("SkullOwner").result();
        Optional<Dynamic<T>> optional1 = dynamic.get("ExtraType").result();
        Optional<Dynamic<T>> optional2 = optional.or(() -> {
            return optional1;
        });

        if (optional2.isEmpty()) {
            return dynamic;
        } else {
            dynamic = dynamic.remove("SkullOwner").remove("ExtraType");
            dynamic = dynamic.set("profile", ItemStackComponentizationFix.fixProfile((Dynamic) optional2.get()));
            return dynamic;
        }
    }
}
