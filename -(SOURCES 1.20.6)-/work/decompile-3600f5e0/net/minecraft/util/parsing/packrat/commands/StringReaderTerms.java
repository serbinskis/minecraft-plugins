package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;

public interface StringReaderTerms {

    static Term<StringReader> word(String s) {
        return new StringReaderTerms.b(s);
    }

    static Term<StringReader> character(char c0) {
        return new StringReaderTerms.a(c0);
    }

    public static record b(String value) implements Term<StringReader> {

        @Override
        public boolean parse(ParseState<StringReader> parsestate, Scope scope, Control control) {
            ((StringReader) parsestate.input()).skipWhitespace();
            int i = parsestate.mark();
            String s = ((StringReader) parsestate.input()).readUnquotedString();

            if (!s.equals(this.value)) {
                parsestate.errorCollector().store(i, (parsestate1) -> {
                    return Stream.of(this.value);
                }, CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(this.value));
                return false;
            } else {
                return true;
            }
        }
    }

    public static record a(char value) implements Term<StringReader> {

        @Override
        public boolean parse(ParseState<StringReader> parsestate, Scope scope, Control control) {
            ((StringReader) parsestate.input()).skipWhitespace();
            int i = parsestate.mark();

            if (((StringReader) parsestate.input()).canRead() && ((StringReader) parsestate.input()).read() == this.value) {
                return true;
            } else {
                parsestate.errorCollector().store(i, (parsestate1) -> {
                    return Stream.of(String.valueOf(this.value));
                }, CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(this.value));
                return false;
            }
        }
    }
}
