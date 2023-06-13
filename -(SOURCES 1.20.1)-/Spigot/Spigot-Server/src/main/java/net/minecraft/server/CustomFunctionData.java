package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CustomFunction;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.level.GameRules;

public class CustomFunctionData {

    private static final IChatBaseComponent NO_RECURSIVE_TRACES = IChatBaseComponent.translatable("commands.debug.function.noRecursion");
    private static final MinecraftKey TICK_FUNCTION_TAG = new MinecraftKey("tick");
    private static final MinecraftKey LOAD_FUNCTION_TAG = new MinecraftKey("load");
    final MinecraftServer server;
    @Nullable
    private CustomFunctionData.ExecutionContext context;
    private List<CustomFunction> ticking = ImmutableList.of();
    private boolean postReload;
    private CustomFunctionManager library;

    public CustomFunctionData(MinecraftServer minecraftserver, CustomFunctionManager customfunctionmanager) {
        this.server = minecraftserver;
        this.library = customfunctionmanager;
        this.postReload(customfunctionmanager);
    }

    public int getCommandLimit() {
        return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
    }

    public CommandDispatcher<CommandListenerWrapper> getDispatcher() {
        return this.server.vanillaCommandDispatcher.getDispatcher(); // CraftBukkit
    }

    public void tick() {
        if (this.postReload) {
            this.postReload = false;
            Collection<CustomFunction> collection = this.library.getTag(CustomFunctionData.LOAD_FUNCTION_TAG);

            this.executeTagFunctions(collection, CustomFunctionData.LOAD_FUNCTION_TAG);
        }

        this.executeTagFunctions(this.ticking, CustomFunctionData.TICK_FUNCTION_TAG);
    }

    private void executeTagFunctions(Collection<CustomFunction> collection, MinecraftKey minecraftkey) {
        GameProfilerFiller gameprofilerfiller = this.server.getProfiler();

        Objects.requireNonNull(minecraftkey);
        gameprofilerfiller.push(minecraftkey::toString);
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            CustomFunction customfunction = (CustomFunction) iterator.next();

            this.execute(customfunction, this.getGameLoopSender());
        }

        this.server.getProfiler().pop();
    }

    public int execute(CustomFunction customfunction, CommandListenerWrapper commandlistenerwrapper) {
        return this.execute(customfunction, commandlistenerwrapper, (CustomFunctionData.TraceCallbacks) null);
    }

    public int execute(CustomFunction customfunction, CommandListenerWrapper commandlistenerwrapper, @Nullable CustomFunctionData.TraceCallbacks customfunctiondata_tracecallbacks) {
        if (this.context != null) {
            if (customfunctiondata_tracecallbacks != null) {
                this.context.reportError(CustomFunctionData.NO_RECURSIVE_TRACES.getString());
                return 0;
            } else {
                this.context.delayFunctionCall(customfunction, commandlistenerwrapper);
                return 0;
            }
        } else {
            int i;

            try {
                this.context = new CustomFunctionData.ExecutionContext(customfunctiondata_tracecallbacks);
                i = this.context.runTopCommand(customfunction, commandlistenerwrapper);
            } finally {
                this.context = null;
            }

            return i;
        }
    }

    public void replaceLibrary(CustomFunctionManager customfunctionmanager) {
        this.library = customfunctionmanager;
        this.postReload(customfunctionmanager);
    }

    private void postReload(CustomFunctionManager customfunctionmanager) {
        this.ticking = ImmutableList.copyOf(customfunctionmanager.getTag(CustomFunctionData.TICK_FUNCTION_TAG));
        this.postReload = true;
    }

    public CommandListenerWrapper getGameLoopSender() {
        return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    }

    public Optional<CustomFunction> get(MinecraftKey minecraftkey) {
        return this.library.getFunction(minecraftkey);
    }

    public Collection<CustomFunction> getTag(MinecraftKey minecraftkey) {
        return this.library.getTag(minecraftkey);
    }

    public Iterable<MinecraftKey> getFunctionNames() {
        return this.library.getFunctions().keySet();
    }

    public Iterable<MinecraftKey> getTagNames() {
        return this.library.getAvailableTags();
    }

    public interface TraceCallbacks {

        void onCommand(int i, String s);

        void onReturn(int i, String s, int j);

        void onError(int i, String s);

        void onCall(int i, MinecraftKey minecraftkey, int j);
    }

    private class ExecutionContext {

        private int depth;
        @Nullable
        private final CustomFunctionData.TraceCallbacks tracer;
        private final Deque<CustomFunctionData.QueuedCommand> commandQueue = Queues.newArrayDeque();
        private final List<CustomFunctionData.QueuedCommand> nestedCalls = Lists.newArrayList();
        boolean abortCurrentDepth = false;

        ExecutionContext(@Nullable CustomFunctionData.TraceCallbacks customfunctiondata_tracecallbacks) {
            this.tracer = customfunctiondata_tracecallbacks;
        }

        void delayFunctionCall(CustomFunction customfunction, CommandListenerWrapper commandlistenerwrapper) {
            int i = CustomFunctionData.this.getCommandLimit();
            CommandListenerWrapper commandlistenerwrapper1 = this.wrapSender(commandlistenerwrapper);

            if (this.commandQueue.size() + this.nestedCalls.size() < i) {
                this.nestedCalls.add(new CustomFunctionData.QueuedCommand(commandlistenerwrapper1, this.depth, new CustomFunction.d(customfunction)));
            }

        }

        private CommandListenerWrapper wrapSender(CommandListenerWrapper commandlistenerwrapper) {
            IntConsumer intconsumer = commandlistenerwrapper.getReturnValueConsumer();

            return intconsumer instanceof CustomFunctionData.ExecutionContext.AbortingReturnValueConsumer ? commandlistenerwrapper : commandlistenerwrapper.withReturnValueConsumer(new CustomFunctionData.ExecutionContext.AbortingReturnValueConsumer(intconsumer));
        }

        int runTopCommand(CustomFunction customfunction, CommandListenerWrapper commandlistenerwrapper) {
            int i = CustomFunctionData.this.getCommandLimit();
            CommandListenerWrapper commandlistenerwrapper1 = this.wrapSender(commandlistenerwrapper);
            int j = 0;
            CustomFunction.c[] acustomfunction_c = customfunction.getEntries();

            for (int k = acustomfunction_c.length - 1; k >= 0; --k) {
                this.commandQueue.push(new CustomFunctionData.QueuedCommand(commandlistenerwrapper1, 0, acustomfunction_c[k]));
            }

            do {
                if (this.commandQueue.isEmpty()) {
                    return j;
                }

                try {
                    CustomFunctionData.QueuedCommand customfunctiondata_queuedcommand = (CustomFunctionData.QueuedCommand) this.commandQueue.removeFirst();
                    GameProfilerFiller gameprofilerfiller = CustomFunctionData.this.server.getProfiler();

                    Objects.requireNonNull(customfunctiondata_queuedcommand);
                    gameprofilerfiller.push(customfunctiondata_queuedcommand::toString);
                    this.depth = customfunctiondata_queuedcommand.depth;
                    customfunctiondata_queuedcommand.execute(CustomFunctionData.this, this.commandQueue, i, this.tracer);
                    if (this.abortCurrentDepth) {
                        while (!this.commandQueue.isEmpty() && ((CustomFunctionData.QueuedCommand) this.commandQueue.peek()).depth >= this.depth) {
                            this.commandQueue.removeFirst();
                        }

                        this.abortCurrentDepth = false;
                    } else if (!this.nestedCalls.isEmpty()) {
                        List list = Lists.reverse(this.nestedCalls);
                        Deque deque = this.commandQueue;

                        Objects.requireNonNull(this.commandQueue);
                        list.forEach(deque::addFirst);
                    }

                    this.nestedCalls.clear();
                } finally {
                    CustomFunctionData.this.server.getProfiler().pop();
                }

                ++j;
            } while (j < i);

            return j;
        }

        public void reportError(String s) {
            if (this.tracer != null) {
                this.tracer.onError(this.depth, s);
            }

        }

        private class AbortingReturnValueConsumer implements IntConsumer {

            private final IntConsumer wrapped;

            AbortingReturnValueConsumer(IntConsumer intconsumer) {
                this.wrapped = intconsumer;
            }

            public void accept(int i) {
                this.wrapped.accept(i);
                ExecutionContext.this.abortCurrentDepth = true;
            }
        }
    }

    public static class QueuedCommand {

        private final CommandListenerWrapper sender;
        final int depth;
        private final CustomFunction.c entry;

        public QueuedCommand(CommandListenerWrapper commandlistenerwrapper, int i, CustomFunction.c customfunction_c) {
            this.sender = commandlistenerwrapper;
            this.depth = i;
            this.entry = customfunction_c;
        }

        public void execute(CustomFunctionData customfunctiondata, Deque<CustomFunctionData.QueuedCommand> deque, int i, @Nullable CustomFunctionData.TraceCallbacks customfunctiondata_tracecallbacks) {
            try {
                this.entry.execute(customfunctiondata, this.sender, deque, i, this.depth, customfunctiondata_tracecallbacks);
            } catch (CommandSyntaxException commandsyntaxexception) {
                if (customfunctiondata_tracecallbacks != null) {
                    customfunctiondata_tracecallbacks.onError(this.depth, commandsyntaxexception.getRawMessage().getString());
                }
            } catch (Exception exception) {
                if (customfunctiondata_tracecallbacks != null) {
                    customfunctiondata_tracecallbacks.onError(this.depth, exception.getMessage());
                }
            }

        }

        public String toString() {
            return this.entry.toString();
        }
    }
}
