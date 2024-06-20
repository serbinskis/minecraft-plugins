package net.minecraft.util.datafix;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
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

    @SafeVarargs
    public static <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>>... afunction) {
        return (typed) -> {
            Function[] afunction1 = afunction;
            int i = afunction.length;

            for (int j = 0; j < i; ++j) {
                Function<Typed<?>, Typed<?>> function = afunction1[j];

                typed = (Typed) function.apply(typed);
            }

            return typed;
        };
    }
}
