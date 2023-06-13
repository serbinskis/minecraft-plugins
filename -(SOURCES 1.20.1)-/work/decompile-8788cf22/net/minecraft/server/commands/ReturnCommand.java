package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandListenerWrapper;

public class ReturnCommand {

    public ReturnCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("return").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.argument("value", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return setReturn((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "value"));
        })));
    }

    private static int setReturn(CommandListenerWrapper commandlistenerwrapper, int i) {
        commandlistenerwrapper.getReturnValueConsumer().accept(i);
        return i;
    }
}
