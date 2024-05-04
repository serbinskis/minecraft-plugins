package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class HorseBodyArmorItemFix extends NamedEntityWriteReadFix {

    private final String previousBodyArmorTag;
    private final boolean clearArmorItems;

    public HorseBodyArmorItemFix(Schema schema, String s, String s1, boolean flag) {
        super(schema, true, "Horse armor fix for " + s, DataConverterTypes.ENTITY, s);
        this.previousBodyArmorTag = s1;
        this.clearArmorItems = flag;
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional<? extends Dynamic<?>> optional = dynamic.get(this.previousBodyArmorTag).result();

        if (optional.isPresent()) {
            Dynamic<?> dynamic1 = (Dynamic) optional.get();
            Dynamic<T> dynamic2 = dynamic.remove(this.previousBodyArmorTag);

            if (this.clearArmorItems) {
                dynamic2 = dynamic2.update("ArmorItems", (dynamic3) -> {
                    return dynamic3.createList(Streams.mapWithIndex(dynamic3.asStream(), (dynamic4, i) -> {
                        return i == 2L ? dynamic4.emptyMap() : dynamic4;
                    }));
                });
                dynamic2 = dynamic2.update("ArmorDropChances", (dynamic3) -> {
                    return dynamic3.createList(Streams.mapWithIndex(dynamic3.asStream(), (dynamic4, i) -> {
                        return i == 2L ? dynamic4.createFloat(0.085F) : dynamic4;
                    }));
                });
            }

            dynamic2 = dynamic2.set("body_armor_item", dynamic1);
            dynamic2 = dynamic2.set("body_armor_drop_chance", dynamic.createFloat(2.0F));
            return dynamic2;
        } else {
            return dynamic;
        }
    }
}
