package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import net.minecraft.SystemUtils;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.ICommandListener;
import net.minecraft.commands.arguments.item.ArgumentTag;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.TimeRange;
import net.minecraft.util.profiling.MethodProfilerResults;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class CommandDebug {

    static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.debug.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.debug.alreadyRunning"));
    static final SimpleCommandExceptionType NO_RECURSIVE_TRACES = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.debug.function.noRecursion"));
    static final SimpleCommandExceptionType NO_RETURN_RUN = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.debug.function.noReturnRun"));

    public CommandDebug() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("debug").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.literal("start").executes((commandcontext) -> {
            return start((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("stop").executes((commandcontext) -> {
            return stop((CommandListenerWrapper) commandcontext.getSource());
        }))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("function").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.argument("name", ArgumentTag.functions()).suggests(CommandFunction.SUGGEST_FUNCTION).executes(new CommandDebug.a()))));
    }

    private static int start(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        MinecraftServer minecraftserver = commandlistenerwrapper.getServer();

        if (minecraftserver.isTimeProfilerRunning()) {
            throw CommandDebug.ERROR_ALREADY_RUNNING.create();
        } else {
            minecraftserver.startTimeProfiler();
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.debug.started");
            }, true);
            return 0;
        }
    }

    private static int stop(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        MinecraftServer minecraftserver = commandlistenerwrapper.getServer();

        if (!minecraftserver.isTimeProfilerRunning()) {
            throw CommandDebug.ERROR_NOT_RUNNING.create();
        } else {
            MethodProfilerResults methodprofilerresults = minecraftserver.stopTimeProfiler();
            double d0 = (double) methodprofilerresults.getNanoDuration() / (double) TimeRange.NANOSECONDS_PER_SECOND;
            double d1 = (double) methodprofilerresults.getTickDuration() / d0;

            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d0), methodprofilerresults.getTickDuration(), String.format(Locale.ROOT, "%.2f", d1));
            }, true);
            return (int) d1;
        }
    }

    private static class a extends CustomCommandExecutor.b<CommandListenerWrapper> implements CustomCommandExecutor.a<CommandListenerWrapper> {

        a() {}

        public void runGuarded(CommandListenerWrapper commandlistenerwrapper, ContextChain<CommandListenerWrapper> contextchain, ChainModifiers chainmodifiers, ExecutionControl<CommandListenerWrapper> executioncontrol) throws CommandSyntaxException {
            if (chainmodifiers.isReturn()) {
                throw CommandDebug.NO_RETURN_RUN.create();
            } else if (executioncontrol.tracer() != null) {
                throw CommandDebug.NO_RECURSIVE_TRACES.create();
            } else {
                CommandContext<CommandListenerWrapper> commandcontext = contextchain.getTopContext();
                Collection<net.minecraft.commands.functions.CommandFunction<CommandListenerWrapper>> collection = ArgumentTag.getFunctions(commandcontext, "name");
                MinecraftServer minecraftserver = commandlistenerwrapper.getServer();
                String s = "debug-trace-" + SystemUtils.getFilenameFormattedDateTime() + ".txt";
                CommandDispatcher<CommandListenerWrapper> commanddispatcher = commandlistenerwrapper.getServer().getFunctions().getDispatcher();
                int i = 0;

                try {
                    Path path = minecraftserver.getFile("debug").toPath();

                    Files.createDirectories(path);
                    final PrintWriter printwriter = new PrintWriter(Files.newBufferedWriter(path.resolve(s), StandardCharsets.UTF_8));
                    CommandDebug.b commanddebug_b = new CommandDebug.b(printwriter);

                    executioncontrol.tracer(commanddebug_b);
                    Iterator iterator = collection.iterator();

                    while (iterator.hasNext()) {
                        final net.minecraft.commands.functions.CommandFunction<CommandListenerWrapper> net_minecraft_commands_functions_commandfunction = (net.minecraft.commands.functions.CommandFunction) iterator.next();

                        try {
                            CommandListenerWrapper commandlistenerwrapper1 = commandlistenerwrapper.withSource(commanddebug_b).withMaximumPermission(2);
                            InstantiatedFunction<CommandListenerWrapper> instantiatedfunction = net_minecraft_commands_functions_commandfunction.instantiate((NBTTagCompound) null, commanddispatcher);

                            executioncontrol.queueNext((new CallFunction<CommandListenerWrapper>(this, instantiatedfunction, CommandResultCallback.EMPTY, false) {
                                public void execute(CommandListenerWrapper commandlistenerwrapper2, ExecutionContext<CommandListenerWrapper> executioncontext, Frame frame) {
                                    printwriter.println(net_minecraft_commands_functions_commandfunction.id());
                                    super.execute((ExecutionCommandSource) commandlistenerwrapper2, executioncontext, frame);
                                }
                            }).bind(commandlistenerwrapper1));
                            i += instantiatedfunction.entries().size();
                        } catch (FunctionInstantiationException functioninstantiationexception) {
                            commandlistenerwrapper.sendFailure(functioninstantiationexception.messageComponent());
                        }
                    }
                } catch (IOException | UncheckedIOException uncheckedioexception) {
                    CommandDebug.LOGGER.warn("Tracing failed", uncheckedioexception);
                    commandlistenerwrapper.sendFailure(IChatBaseComponent.translatable("commands.debug.function.traceFailed"));
                }

                executioncontrol.queueNext((executioncontext, frame) -> {
                    if (collection.size() == 1) {
                        commandlistenerwrapper.sendSuccess(() -> {
                            return IChatBaseComponent.translatable("commands.debug.function.success.single", i, IChatBaseComponent.translationArg(((net.minecraft.commands.functions.CommandFunction) collection.iterator().next()).id()), s);
                        }, true);
                    } else {
                        commandlistenerwrapper.sendSuccess(() -> {
                            return IChatBaseComponent.translatable("commands.debug.function.success.multiple", i, collection.size(), s);
                        }, true);
                    }

                });
            }
        }
    }

    private static class b implements ICommandListener, TraceCallbacks {

        public static final int INDENT_OFFSET = 1;
        private final PrintWriter output;
        private int lastIndent;
        private boolean waitingForResult;

        b(PrintWriter printwriter) {
            this.output = printwriter;
        }

        private void indentAndSave(int i) {
            this.printIndent(i);
            this.lastIndent = i;
        }

        private void printIndent(int i) {
            for (int j = 0; j < i + 1; ++j) {
                this.output.write("    ");
            }

        }

        private void newLine() {
            if (this.waitingForResult) {
                this.output.println();
                this.waitingForResult = false;
            }

        }

        @Override
        public void onCommand(int i, String s) {
            this.newLine();
            this.indentAndSave(i);
            this.output.print("[C] ");
            this.output.print(s);
            this.waitingForResult = true;
        }

        @Override
        public void onReturn(int i, String s, int j) {
            if (this.waitingForResult) {
                this.output.print(" -> ");
                this.output.println(j);
                this.waitingForResult = false;
            } else {
                this.indentAndSave(i);
                this.output.print("[R = ");
                this.output.print(j);
                this.output.print("] ");
                this.output.println(s);
            }

        }

        @Override
        public void onCall(int i, MinecraftKey minecraftkey, int j) {
            this.newLine();
            this.indentAndSave(i);
            this.output.print("[F] ");
            this.output.print(minecraftkey);
            this.output.print(" size=");
            this.output.println(j);
        }

        @Override
        public void onError(String s) {
            this.newLine();
            this.indentAndSave(this.lastIndent + 1);
            this.output.print("[E] ");
            this.output.print(s);
        }

        @Override
        public void sendSystemMessage(IChatBaseComponent ichatbasecomponent) {
            this.newLine();
            this.printIndent(this.lastIndent + 1);
            this.output.print("[M] ");
            this.output.println(ichatbasecomponent.getString());
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        @Override
        public boolean alwaysAccepts() {
            return true;
        }

        @Override
        public void close() {
            IOUtils.closeQuietly(this.output);
        }
    }
}
