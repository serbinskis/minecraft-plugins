package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CustomFunction;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.commands.arguments.ArgumentNBTTag;
import net.minecraft.commands.arguments.item.ArgumentTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.CustomFunctionData;
import net.minecraft.server.commands.data.CommandData;
import net.minecraft.server.commands.data.CommandDataAccessor;
import org.apache.commons.lang3.mutable.MutableObject;

public class CommandFunction {

    private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatable("commands.function.error.argument_not_compound", object);
    });
    public static final SuggestionProvider<CommandListenerWrapper> SUGGEST_FUNCTION = (commandcontext, suggestionsbuilder) -> {
        CustomFunctionData customfunctiondata = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getFunctions();

        ICompletionProvider.suggestResource(customfunctiondata.getTagNames(), suggestionsbuilder, "#");
        return ICompletionProvider.suggestResource(customfunctiondata.getFunctionNames(), suggestionsbuilder);
    };

    public CommandFunction() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder = net.minecraft.commands.CommandDispatcher.literal("with");
        Iterator iterator = CommandData.SOURCE_PROVIDERS.iterator();

        while (iterator.hasNext()) {
            CommandData.c commanddata_c = (CommandData.c) iterator.next();

            commanddata_c.wrap(literalargumentbuilder, (argumentbuilder) -> {
                return argumentbuilder.executes((commandcontext) -> {
                    return runFunction((CommandListenerWrapper) commandcontext.getSource(), ArgumentTag.getFunctions(commandcontext, "name"), commanddata_c.access(commandcontext).getData());
                }).then(net.minecraft.commands.CommandDispatcher.argument("path", ArgumentNBTKey.nbtPath()).executes((commandcontext) -> {
                    return runFunction((CommandListenerWrapper) commandcontext.getSource(), ArgumentTag.getFunctions(commandcontext, "name"), getArgumentTag(ArgumentNBTKey.getPath(commandcontext, "path"), commanddata_c.access(commandcontext)));
                }));
            });
        }

        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("function").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("name", ArgumentTag.functions()).suggests(CommandFunction.SUGGEST_FUNCTION).executes((commandcontext) -> {
            return runFunction((CommandListenerWrapper) commandcontext.getSource(), ArgumentTag.getFunctions(commandcontext, "name"), (NBTTagCompound) null);
        })).then(net.minecraft.commands.CommandDispatcher.argument("arguments", ArgumentNBTTag.compoundTag()).executes((commandcontext) -> {
            return runFunction((CommandListenerWrapper) commandcontext.getSource(), ArgumentTag.getFunctions(commandcontext, "name"), ArgumentNBTTag.getCompoundTag(commandcontext, "arguments"));
        }))).then(literalargumentbuilder)));
    }

    private static NBTTagCompound getArgumentTag(ArgumentNBTKey.g argumentnbtkey_g, CommandDataAccessor commanddataaccessor) throws CommandSyntaxException {
        NBTBase nbtbase = CommandData.getSingleTag(argumentnbtkey_g, commanddataaccessor);

        if (nbtbase instanceof NBTTagCompound) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) nbtbase;

            return nbttagcompound;
        } else {
            throw CommandFunction.ERROR_ARGUMENT_NOT_COMPOUND.create(nbtbase.getType().getName());
        }
    }

    private static int runFunction(CommandListenerWrapper commandlistenerwrapper, Collection<CustomFunction> collection, @Nullable NBTTagCompound nbttagcompound) {
        int i = 0;
        boolean flag = false;
        boolean flag1 = false;
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            CustomFunction customfunction = (CustomFunction) iterator.next();

            try {
                CommandFunction.a commandfunction_a = runFunction(commandlistenerwrapper, customfunction, nbttagcompound);

                i += commandfunction_a.value();
                flag |= commandfunction_a.isReturn();
                flag1 = true;
            } catch (FunctionInstantiationException functioninstantiationexception) {
                commandlistenerwrapper.sendFailure(functioninstantiationexception.messageComponent());
            }
        }

        if (flag1) {
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
        }

        return i;
    }

    public static CommandFunction.a runFunction(CommandListenerWrapper commandlistenerwrapper, CustomFunction customfunction, @Nullable NBTTagCompound nbttagcompound) throws FunctionInstantiationException {
        MutableObject<CommandFunction.a> mutableobject = new MutableObject();
        int i = commandlistenerwrapper.getServer().getFunctions().execute(customfunction, commandlistenerwrapper.withSuppressedOutput().withMaximumPermission(2).withReturnValueConsumer((j) -> {
            mutableobject.setValue(new CommandFunction.a(j, true));
        }), (CustomFunctionData.TraceCallbacks) null, nbttagcompound);
        CommandFunction.a commandfunction_a = (CommandFunction.a) mutableobject.getValue();

        return commandfunction_a != null ? commandfunction_a : new CommandFunction.a(i, false);
    }

    public static record a(int value, boolean isReturn) {

    }
}
