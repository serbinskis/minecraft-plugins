package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.OptionalInt;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CustomFunction;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.item.ArgumentTag;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.CustomFunctionData;
import org.apache.commons.lang3.mutable.MutableObject;

public class CommandFunction {

    public static final SuggestionProvider<CommandListenerWrapper> SUGGEST_FUNCTION = (commandcontext, suggestionsbuilder) -> {
        CustomFunctionData customfunctiondata = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getFunctions();

        ICompletionProvider.suggestResource(customfunctiondata.getTagNames(), suggestionsbuilder, "#");
        return ICompletionProvider.suggestResource(customfunctiondata.getFunctionNames(), suggestionsbuilder);
    };

    public CommandFunction() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("function").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.argument("name", ArgumentTag.functions()).suggests(CommandFunction.SUGGEST_FUNCTION).executes((commandcontext) -> {
            return runFunction((CommandListenerWrapper) commandcontext.getSource(), ArgumentTag.getFunctions(commandcontext, "name"));
        })));
    }

    private static int runFunction(CommandListenerWrapper commandlistenerwrapper, Collection<CustomFunction> collection) {
        int i = 0;
        boolean flag = false;

        OptionalInt optionalint;

        for (Iterator iterator = collection.iterator(); iterator.hasNext(); flag |= optionalint.isPresent()) {
            CustomFunction customfunction = (CustomFunction) iterator.next();
            MutableObject<OptionalInt> mutableobject = new MutableObject(OptionalInt.empty());
            int j = commandlistenerwrapper.getServer().getFunctions().execute(customfunction, commandlistenerwrapper.withSuppressedOutput().withMaximumPermission(2).withReturnValueConsumer((k) -> {
                mutableobject.setValue(OptionalInt.of(k));
            }));

            optionalint = (OptionalInt) mutableobject.getValue();
            i += optionalint.orElse(j);
        }

        if (collection.size() == 1) {
            if (flag) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.function.success.single.result", i, ((CustomFunction) collection.iterator().next()).getId());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.function.success.single", i, ((CustomFunction) collection.iterator().next()).getId());
                }, true);
            }
        } else if (flag) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.function.success.multiple.result", collection.size());
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.function.success.multiple", i, collection.size());
            }, true);
        }

        return i;
    }
}
