package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class DropInvalidSignDataFix extends DataConverterNamedEntity {

    private static final String[] FIELDS_TO_DROP = new String[]{"Text1", "Text2", "Text3", "Text4", "FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4", "Color", "GlowingText"};

    public DropInvalidSignDataFix(Schema schema, String s, String s1) {
        super(schema, false, s, DataConverterTypes.BLOCK_ENTITY, s1);
    }

    private static <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        dynamic = dynamic.update("front_text", DropInvalidSignDataFix::fixText);
        dynamic = dynamic.update("back_text", DropInvalidSignDataFix::fixText);
        String[] astring = DropInvalidSignDataFix.FIELDS_TO_DROP;
        int i = astring.length;

        for (int j = 0; j < i; ++j) {
            String s = astring[j];

            dynamic = dynamic.remove(s);
        }

        return dynamic;
    }

    private static <T> Dynamic<T> fixText(Dynamic<T> dynamic) {
        boolean flag = dynamic.get("_filtered_correct").asBoolean(false);

        if (flag) {
            return dynamic.remove("_filtered_correct");
        } else {
            Optional<Stream<Dynamic<T>>> optional = dynamic.get("filtered_messages").asStreamOpt().result();

            if (optional.isEmpty()) {
                return dynamic;
            } else {
                Dynamic<T> dynamic1 = ComponentDataFixUtils.createEmptyComponent(dynamic.getOps());
                List<Dynamic<T>> list = ((Stream) dynamic.get("messages").asStreamOpt().result().orElse(Stream.of())).toList();
                List<Dynamic<T>> list1 = Streams.mapWithIndex((Stream) optional.get(), (dynamic2, i) -> {
                    Dynamic<T> dynamic3 = i < (long) list.size() ? (Dynamic) list.get((int) i) : dynamic1;

                    return dynamic2.equals(dynamic1) ? dynamic3 : dynamic2;
                }).toList();

                return list1.stream().allMatch((dynamic2) -> {
                    return dynamic2.equals(dynamic1);
                }) ? dynamic.remove("filtered_messages") : dynamic.set("filtered_messages", dynamic.createList(list1.stream()));
            }
        }
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), DropInvalidSignDataFix::fix);
    }
}
