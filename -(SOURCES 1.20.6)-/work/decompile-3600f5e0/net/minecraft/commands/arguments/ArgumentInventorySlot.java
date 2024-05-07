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
import net.minecraft.commands.ParserUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public class ArgumentInventorySlot implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "weapon");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("slot.unknown", object);
    });
    private static final DynamicCommandExceptionType ERROR_ONLY_SINGLE_SLOT_ALLOWED = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("slot.only_single_allowed", object);
    });

    public ArgumentInventorySlot() {}

    public static ArgumentInventorySlot slot() {
        return new ArgumentInventorySlot();
    }

    public static int getSlot(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (Integer) commandcontext.getArgument(s, Integer.class);
    }

    public Integer parse(StringReader stringreader) throws CommandSyntaxException {
        String s = ParserUtils.readWhile(stringreader, (c0) -> {
            return c0 != ' ';
        });
        SlotRange slotrange = SlotRanges.nameToIds(s);

        if (slotrange == null) {
            throw ArgumentInventorySlot.ERROR_UNKNOWN_SLOT.createWithContext(stringreader, s);
        } else if (slotrange.size() != 1) {
            throw ArgumentInventorySlot.ERROR_ONLY_SINGLE_SLOT_ALLOWED.createWithContext(stringreader, s);
        } else {
            return slotrange.slots().getInt(0);
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        return ICompletionProvider.suggest(SlotRanges.singleSlotNames(), suggestionsbuilder);
    }

    public Collection<String> getExamples() {
        return ArgumentInventorySlot.EXAMPLES;
    }
}
