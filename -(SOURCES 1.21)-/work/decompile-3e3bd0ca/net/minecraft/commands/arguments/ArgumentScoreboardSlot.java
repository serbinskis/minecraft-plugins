package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.scores.DisplaySlot;

public class ArgumentScoreboardSlot implements ArgumentType<DisplaySlot> {

    private static final Collection<String> EXAMPLES = Arrays.asList("sidebar", "foo.bar");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.scoreboardDisplaySlot.invalid", object);
    });

    private ArgumentScoreboardSlot() {}

    public static ArgumentScoreboardSlot displaySlot() {
        return new ArgumentScoreboardSlot();
    }

    public static DisplaySlot getDisplaySlot(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (DisplaySlot) commandcontext.getArgument(s, DisplaySlot.class);
    }

    public DisplaySlot parse(StringReader stringreader) throws CommandSyntaxException {
        String s = stringreader.readUnquotedString();
        DisplaySlot displayslot = (DisplaySlot) DisplaySlot.CODEC.byName(s);

        if (displayslot == null) {
            throw ArgumentScoreboardSlot.ERROR_INVALID_VALUE.createWithContext(stringreader, s);
        } else {
            return displayslot;
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        return ICompletionProvider.suggest(Arrays.stream(DisplaySlot.values()).map(DisplaySlot::getSerializedName), suggestionsbuilder);
    }

    public Collection<String> getExamples() {
        return ArgumentScoreboardSlot.EXAMPLES;
    }
}
