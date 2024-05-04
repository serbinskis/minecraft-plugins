package net.minecraft.util.datafix;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.IntStream;

public class ExtraDataFixUtils {

    public ExtraDataFixUtils() {}

    public static Dynamic<?> fixBlockPos(Dynamic<?> dynamic) {
        Optional<Number> optional = dynamic.get("X").asNumber().result();
        Optional<Number> optional1 = dynamic.get("Y").asNumber().result();
        Optional<Number> optional2 = dynamic.get("Z").asNumber().result();

        return !optional.isEmpty() && !optional1.isEmpty() && !optional2.isEmpty() ? dynamic.createIntList(IntStream.of(new int[]{((Number) optional.get()).intValue(), ((Number) optional1.get()).intValue(), ((Number) optional2.get()).intValue()})) : dynamic;
    }

    public static <T, R> Typed<R> cast(Type<R> type, Typed<T> typed) {
        return new Typed(type, typed.getOps(), typed.getValue());
    }
}
