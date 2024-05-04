package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import java.util.Optional;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class TagParseRule implements Rule<StringReader, NBTBase> {

    public static final Rule<StringReader, NBTBase> INSTANCE = new TagParseRule();

    private TagParseRule() {}

    @Override
    public Optional<NBTBase> parse(ParseState<StringReader> parsestate) {
        ((StringReader) parsestate.input()).skipWhitespace();
        int i = parsestate.mark();

        try {
            return Optional.of((new MojangsonParser((StringReader) parsestate.input())).readValue());
        } catch (Exception exception) {
            parsestate.errorCollector().store(i, exception);
            return Optional.empty();
        }
    }
}
