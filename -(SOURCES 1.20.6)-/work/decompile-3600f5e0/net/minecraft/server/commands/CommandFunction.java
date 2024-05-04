package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.commands.arguments.ArgumentNBTTag;
import net.minecraft.commands.arguments.item.ArgumentTag;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.CustomFunctionData;
import net.minecraft.server.commands.data.CommandData;
import net.minecraft.server.commands.data.CommandDataAccessor;

public class CommandFunction {

    private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.function.error.argument_not_compound", object);
    });
    static final DynamicCommandExceptionType ERROR_NO_FUNCTIONS = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.function.scheduled.no_functions", object);
    });
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType ERROR_FUNCTION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("commands.function.instantiationFailure", object, object1);
    });
    public static final SuggestionProvider<CommandListenerWrapper> SUGGEST_FUNCTION = (commandcontext, suggestionsbuilder) -> {
        CustomFunctionData customfunctiondata = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getFunctions();

        ICompletionProvider.suggestResource(customfunctiondata.getTagNames(), suggestionsbuilder, "#");
        return ICompletionProvider.suggestResource(customfunctiondata.getFunctionNames(), suggestionsbuilder);
    };
    static final CommandFunction.b<CommandListenerWrapper> FULL_CONTEXT_CALLBACKS = new CommandFunction.b<CommandListenerWrapper>() {
        public void signalResult(CommandListenerWrapper commandlistenerwrapper, MinecraftKey minecraftkey, int i) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.function.result", IChatBaseComponent.translationArg(minecraftkey), i);
            }, true);
        }
    };

    public CommandFunction() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder = net.minecraft.commands.CommandDispatcher.literal("with");
        Iterator iterator = CommandData.SOURCE_PROVIDERS.iterator();

        while (iterator.hasNext()) {
            CommandData.c commanddata_c = (CommandData.c) iterator.next();

            commanddata_c.wrap(literalargumentbuilder, (argumentbuilder) -> {
                return argumentbuilder.executes(new CommandFunction.c() {
                    @Override
                    protected NBTTagCompound arguments(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException {
                        return commanddata_c.access(commandcontext).getData();
                    }
                }).then(net.minecraft.commands.CommandDispatcher.argument("path", ArgumentNBTKey.nbtPath()).executes(new CommandFunction.c() {
                    @Override
                    protected NBTTagCompound arguments(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException {
                        return CommandFunction.getArgumentTag(ArgumentNBTKey.getPath(commandcontext, "path"), commanddata_c.access(commandcontext));
                    }
                }));
            });
        }

        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("function").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("name", ArgumentTag.functions()).suggests(CommandFunction.SUGGEST_FUNCTION).executes(new CommandFunction.c() {
            @Nullable
            @Override
            protected NBTTagCompound arguments(CommandContext<CommandListenerWrapper> commandcontext) {
                return null;
            }
        })).then(net.minecraft.commands.CommandDispatcher.argument("arguments", ArgumentNBTTag.compoundTag()).executes(new CommandFunction.c() {
            @Override
            protected NBTTagCompound arguments(CommandContext<CommandListenerWrapper> commandcontext) {
                return ArgumentNBTTag.getCompoundTag(commandcontext, "arguments");
            }
        }))).then(literalargumentbuilder)));
    }

    static NBTTagCompound getArgumentTag(ArgumentNBTKey.g argumentnbtkey_g, CommandDataAccessor commanddataaccessor) throws CommandSyntaxException {
        NBTBase nbtbase = CommandData.getSingleTag(argumentnbtkey_g, commanddataaccessor);

        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
            return nbttagcompound;
        } else {
            throw CommandFunction.ERROR_ARGUMENT_NOT_COMPOUND.create(nbtbase.getType().getName());
        }
    }

    public static CommandListenerWrapper modifySenderForExecution(CommandListenerWrapper commandlistenerwrapper) {
        return commandlistenerwrapper.withSuppressedOutput().withMaximumPermission(2);
    }

    public static <T extends ExecutionCommandSource<T>> void queueFunctions(Collection<net.minecraft.commands.functions.CommandFunction<T>> collection, @Nullable NBTTagCompound nbttagcompound, T t0, T t1, ExecutionControl<T> executioncontrol, CommandFunction.b<T> commandfunction_b, ChainModifiers chainmodifiers) throws CommandSyntaxException {
        if (chainmodifiers.isReturn()) {
            queueFunctionsAsReturn(collection, nbttagcompound, t0, t1, executioncontrol, commandfunction_b);
        } else {
            queueFunctionsNoReturn(collection, nbttagcompound, t0, t1, executioncontrol, commandfunction_b);
        }

    }

    private static <T extends ExecutionCommandSource<T>> void instantiateAndQueueFunctions(@Nullable NBTTagCompound nbttagcompound, ExecutionControl<T> executioncontrol, CommandDispatcher<T> commanddispatcher, T t0, net.minecraft.commands.functions.CommandFunction<T> net_minecraft_commands_functions_commandfunction, MinecraftKey minecraftkey, CommandResultCallback commandresultcallback, boolean flag) throws CommandSyntaxException {
        try {
            InstantiatedFunction<T> instantiatedfunction = net_minecraft_commands_functions_commandfunction.instantiate(nbttagcompound, commanddispatcher);

            executioncontrol.queueNext((new CallFunction<>(instantiatedfunction, commandresultcallback, flag)).bind(t0));
        } catch (FunctionInstantiationException functioninstantiationexception) {
            throw CommandFunction.ERROR_FUNCTION_INSTANTATION_FAILURE.create(minecraftkey, functioninstantiationexception.messageComponent());
        }
    }

    private static <T extends ExecutionCommandSource<T>> CommandResultCallback decorateOutputIfNeeded(T t0, CommandFunction.b<T> commandfunction_b, MinecraftKey minecraftkey, CommandResultCallback commandresultcallback) {
        return t0.isSilent() ? commandresultcallback : (flag, i) -> {
            commandfunction_b.signalResult(t0, minecraftkey, i);
            commandresultcallback.onResult(flag, i);
        };
    }

    private static <T extends ExecutionCommandSource<T>> void queueFunctionsAsReturn(Collection<net.minecraft.commands.functions.CommandFunction<T>> collection, @Nullable NBTTagCompound nbttagcompound, T t0, T t1, ExecutionControl<T> executioncontrol, CommandFunction.b<T> commandfunction_b) throws CommandSyntaxException {
        CommandDispatcher<T> commanddispatcher = t0.dispatcher();
        T t2 = t1.clearCallbacks();
        CommandResultCallback commandresultcallback = CommandResultCallback.chain(t0.callback(), executioncontrol.currentFrame().returnValueConsumer());
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            net.minecraft.commands.functions.CommandFunction<T> net_minecraft_commands_functions_commandfunction = (net.minecraft.commands.functions.CommandFunction) iterator.next();
            MinecraftKey minecraftkey = net_minecraft_commands_functions_commandfunction.id();
            CommandResultCallback commandresultcallback1 = decorateOutputIfNeeded(t0, commandfunction_b, minecraftkey, commandresultcallback);

            instantiateAndQueueFunctions(nbttagcompound, executioncontrol, commanddispatcher, t2, net_minecraft_commands_functions_commandfunction, minecraftkey, commandresultcallback1, true);
        }

        executioncontrol.queueNext(FallthroughTask.instance());
    }

    private static <T extends ExecutionCommandSource<T>> void queueFunctionsNoReturn(Collection<net.minecraft.commands.functions.CommandFunction<T>> collection, @Nullable NBTTagCompound nbttagcompound, T t0, T t1, ExecutionControl<T> executioncontrol, CommandFunction.b<T> commandfunction_b) throws CommandSyntaxException {
        CommandDispatcher<T> commanddispatcher = t0.dispatcher();
        T t2 = t1.clearCallbacks();
        CommandResultCallback commandresultcallback = t0.callback();

        if (!collection.isEmpty()) {
            if (collection.size() == 1) {
                net.minecraft.commands.functions.CommandFunction<T> net_minecraft_commands_functions_commandfunction = (net.minecraft.commands.functions.CommandFunction) collection.iterator().next();
                MinecraftKey minecraftkey = net_minecraft_commands_functions_commandfunction.id();
                CommandResultCallback commandresultcallback1 = decorateOutputIfNeeded(t0, commandfunction_b, minecraftkey, commandresultcallback);

                instantiateAndQueueFunctions(nbttagcompound, executioncontrol, commanddispatcher, t2, net_minecraft_commands_functions_commandfunction, minecraftkey, commandresultcallback1, false);
            } else if (commandresultcallback == CommandResultCallback.EMPTY) {
                Iterator iterator = collection.iterator();

                while (iterator.hasNext()) {
                    net.minecraft.commands.functions.CommandFunction<T> net_minecraft_commands_functions_commandfunction1 = (net.minecraft.commands.functions.CommandFunction) iterator.next();
                    MinecraftKey minecraftkey1 = net_minecraft_commands_functions_commandfunction1.id();
                    CommandResultCallback commandresultcallback2 = decorateOutputIfNeeded(t0, commandfunction_b, minecraftkey1, commandresultcallback);

                    instantiateAndQueueFunctions(nbttagcompound, executioncontrol, commanddispatcher, t2, net_minecraft_commands_functions_commandfunction1, minecraftkey1, commandresultcallback2, false);
                }
            } else {
                class a {

                    boolean anyResult;
                    int sum;

                    a() {}

                    public void add(int i) {
                        this.anyResult = true;
                        this.sum += i;
                    }
                }

                a a0 = new a();
                CommandResultCallback commandresultcallback3 = (flag, i) -> {
                    a0.add(i);
                };
                Iterator iterator1 = collection.iterator();

                while (iterator1.hasNext()) {
                    net.minecraft.commands.functions.CommandFunction<T> net_minecraft_commands_functions_commandfunction2 = (net.minecraft.commands.functions.CommandFunction) iterator1.next();
                    MinecraftKey minecraftkey2 = net_minecraft_commands_functions_commandfunction2.id();
                    CommandResultCallback commandresultcallback4 = decorateOutputIfNeeded(t0, commandfunction_b, minecraftkey2, commandresultcallback3);

                    instantiateAndQueueFunctions(nbttagcompound, executioncontrol, commanddispatcher, t2, net_minecraft_commands_functions_commandfunction2, minecraftkey2, commandresultcallback4, false);
                }

                executioncontrol.queueNext((executioncontext, frame) -> {
                    if (a0.anyResult) {
                        commandresultcallback.onSuccess(a0.sum);
                    }

                });
            }

        }
    }

    public interface b<T> {

        void signalResult(T t0, MinecraftKey minecraftkey, int i);
    }

    private abstract static class c extends CustomCommandExecutor.b<CommandListenerWrapper> implements CustomCommandExecutor.a<CommandListenerWrapper> {

        c() {}

        @Nullable
        protected abstract NBTTagCompound arguments(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException;

        public void runGuarded(CommandListenerWrapper commandlistenerwrapper, ContextChain<CommandListenerWrapper> contextchain, ChainModifiers chainmodifiers, ExecutionControl<CommandListenerWrapper> executioncontrol) throws CommandSyntaxException {
            CommandContext<CommandListenerWrapper> commandcontext = contextchain.getTopContext().copyFor(commandlistenerwrapper);
            Pair<MinecraftKey, Collection<net.minecraft.commands.functions.CommandFunction<CommandListenerWrapper>>> pair = ArgumentTag.getFunctionCollection(commandcontext, "name");
            Collection<net.minecraft.commands.functions.CommandFunction<CommandListenerWrapper>> collection = (Collection) pair.getSecond();

            if (collection.isEmpty()) {
                throw CommandFunction.ERROR_NO_FUNCTIONS.create(IChatBaseComponent.translationArg((MinecraftKey) pair.getFirst()));
            } else {
                NBTTagCompound nbttagcompound = this.arguments(commandcontext);
                CommandListenerWrapper commandlistenerwrapper1 = CommandFunction.modifySenderForExecution(commandlistenerwrapper);

                if (collection.size() == 1) {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.function.scheduled.single", IChatBaseComponent.translationArg(((net.minecraft.commands.functions.CommandFunction) collection.iterator().next()).id()));
                    }, true);
                } else {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.function.scheduled.multiple", ChatComponentUtils.formatList(collection.stream().map(net.minecraft.commands.functions.CommandFunction::id).toList(), IChatBaseComponent::translationArg));
                    }, true);
                }

                CommandFunction.queueFunctions(collection, nbttagcompound, commandlistenerwrapper, commandlistenerwrapper1, executioncontrol, CommandFunction.FULL_CONTEXT_CALLBACKS, chainmodifiers);
            }
        }
    }
}
