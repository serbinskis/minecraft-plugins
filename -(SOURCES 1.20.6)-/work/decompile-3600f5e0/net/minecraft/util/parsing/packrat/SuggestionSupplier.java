package net.minecraft.util.parsing.packrat;

import java.util.stream.Stream;

public interface SuggestionSupplier<S> {

    Stream<String> possibleValues(ParseState<S> parsestate);

    static <S> SuggestionSupplier<S> empty() {
        return (parsestate) -> {
            return Stream.empty();
        };
    }
}
