package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.ParserUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public class SlotsArgument implements ArgumentType<SlotRange> {

    private static final Collection<String> EXAMPLES = List.of("container.*", "container.5", "weapon");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("slot.unknown", object);
    });

    public SlotsArgument() {}

    public static SlotsArgument slots() {
        return new SlotsArgument();
    }

    public static SlotRange getSlots(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (SlotRange) commandcontext.getArgument(s, SlotRange.class);
    }

    public SlotRange parse(StringReader stringreader) throws CommandSyntaxException {
        String s = ParserUtils.readWhile(stringreader, (c0) -> {
            return c0 != ' ';
        });
        SlotRange slotrange = SlotRanges.nameToIds(s);

        if (slotrange == null) {
            throw SlotsArgument.ERROR_UNKNOWN_SLOT.createWithContext(stringreader, s);
        } else {
            return slotrange;
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        return ICompletionProvider.suggest(SlotRanges.allNames(), suggestionsbuilder);
    }

    public Collection<String> getExamples() {
        return SlotsArgument.EXAMPLES;
    }
}
