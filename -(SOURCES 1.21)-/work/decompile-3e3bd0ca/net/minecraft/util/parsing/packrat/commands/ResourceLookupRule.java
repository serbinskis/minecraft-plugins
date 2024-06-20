package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import java.util.Optional;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public abstract class ResourceLookupRule<C, V> implements Rule<StringReader, V>, ResourceSuggestion {

    private final Atom<MinecraftKey> idParser;
    protected final C context;

    protected ResourceLookupRule(Atom<MinecraftKey> atom, C c0) {
        this.idParser = atom;
        this.context = c0;
    }

    @Override
    public Optional<V> parse(ParseState<StringReader> parsestate) {
        ((StringReader) parsestate.input()).skipWhitespace();
        int i = parsestate.mark();
        Optional<MinecraftKey> optional = parsestate.parse(this.idParser);

        if (optional.isPresent()) {
            try {
                return Optional.of(this.validateElement((ImmutableStringReader) parsestate.input(), (MinecraftKey) optional.get()));
            } catch (Exception exception) {
                parsestate.errorCollector().store(i, this, exception);
                return Optional.empty();
            }
        } else {
            parsestate.errorCollector().store(i, this, MinecraftKey.ERROR_INVALID.createWithContext((ImmutableStringReader) parsestate.input()));
            return Optional.empty();
        }
    }

    protected abstract V validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception;
}
