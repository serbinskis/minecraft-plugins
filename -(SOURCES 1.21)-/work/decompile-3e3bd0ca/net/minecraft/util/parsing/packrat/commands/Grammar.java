package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;

public record Grammar<T>(Dictionary<StringReader> rules, Atom<T> top) {

    public Optional<T> parse(ParseState<StringReader> parsestate) {
        return parsestate.parseTopRule(this.top);
    }

    public T parseForCommands(StringReader stringreader) throws CommandSyntaxException {
        ErrorCollector.a<StringReader> errorcollector_a = new ErrorCollector.a<>();
        StringReaderParserState stringreaderparserstate = new StringReaderParserState(this.rules(), errorcollector_a, stringreader);
        Optional<T> optional = this.parse(stringreaderparserstate);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            List<Exception> list = errorcollector_a.entries().stream().mapMulti((errorentry, consumer) -> {
                Object object = errorentry.reason();

                if (object instanceof Exception exception) {
                    consumer.accept(exception);
                }

            }).toList();
            Iterator iterator = list.iterator();

            Exception exception;

            do {
                if (!iterator.hasNext()) {
                    if (list.size() == 1) {
                        Object object = list.get(0);

                        if (object instanceof RuntimeException) {
                            RuntimeException runtimeexception = (RuntimeException) object;

                            throw runtimeexception;
                        }
                    }

                    Stream stream = errorcollector_a.entries().stream().map(ErrorEntry::toString);

                    throw new IllegalStateException("Failed to parse: " + (String) stream.collect(Collectors.joining(", ")));
                }

                exception = (Exception) iterator.next();
            } while (!(exception instanceof CommandSyntaxException));

            CommandSyntaxException commandsyntaxexception = (CommandSyntaxException) exception;

            throw commandsyntaxexception;
        }
    }

    public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsbuilder) {
        StringReader stringreader = new StringReader(suggestionsbuilder.getInput());

        stringreader.setCursor(suggestionsbuilder.getStart());
        ErrorCollector.a<StringReader> errorcollector_a = new ErrorCollector.a<>();
        StringReaderParserState stringreaderparserstate = new StringReaderParserState(this.rules(), errorcollector_a, stringreader);

        this.parse(stringreaderparserstate);
        List<ErrorEntry<StringReader>> list = errorcollector_a.entries();

        if (list.isEmpty()) {
            return suggestionsbuilder.buildFuture();
        } else {
            SuggestionsBuilder suggestionsbuilder1 = suggestionsbuilder.createOffset(errorcollector_a.cursor());
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                ErrorEntry<StringReader> errorentry = (ErrorEntry) iterator.next();
                SuggestionSupplier suggestionsupplier = errorentry.suggestions();

                if (suggestionsupplier instanceof ResourceSuggestion) {
                    ResourceSuggestion resourcesuggestion = (ResourceSuggestion) suggestionsupplier;

                    ICompletionProvider.suggestResource(resourcesuggestion.possibleResources(), suggestionsbuilder1);
                } else {
                    ICompletionProvider.suggest(errorentry.suggestions().possibleValues(stringreaderparserstate), suggestionsbuilder1);
                }
            }

            return suggestionsbuilder1.buildFuture();
        }
    }
}
