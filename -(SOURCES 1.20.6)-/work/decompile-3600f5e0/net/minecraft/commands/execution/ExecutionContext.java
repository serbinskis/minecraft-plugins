package net.minecraft.commands.execution;

import com.google.common.collect.Queues;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.util.profiling.GameProfilerFiller;
import org.slf4j.Logger;

public class ExecutionContext<T> implements AutoCloseable {

    private static final int MAX_QUEUE_DEPTH = 10000000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final int commandLimit;
    private final int forkLimit;
    private final GameProfilerFiller profiler;
    @Nullable
    private TraceCallbacks tracer;
    private int commandQuota;
    private boolean queueOverflow;
    private final Deque<CommandQueueEntry<T>> commandQueue = Queues.newArrayDeque();
    private final List<CommandQueueEntry<T>> newTopCommands = new ObjectArrayList();
    private int currentFrameDepth;

    public ExecutionContext(int i, int j, GameProfilerFiller gameprofilerfiller) {
        this.commandLimit = i;
        this.forkLimit = j;
        this.profiler = gameprofilerfiller;
        this.commandQuota = i;
    }

    private static <T extends ExecutionCommandSource<T>> Frame createTopFrame(ExecutionContext<T> executioncontext, CommandResultCallback commandresultcallback) {
        if (executioncontext.currentFrameDepth == 0) {
            Deque deque = executioncontext.commandQueue;

            Objects.requireNonNull(executioncontext.commandQueue);
            return new Frame(0, commandresultcallback, deque::clear);
        } else {
            int i = executioncontext.currentFrameDepth + 1;

            return new Frame(i, commandresultcallback, executioncontext.frameControlForDepth(i));
        }
    }

    public static <T extends ExecutionCommandSource<T>> void queueInitialFunctionCall(ExecutionContext<T> executioncontext, InstantiatedFunction<T> instantiatedfunction, T t0, CommandResultCallback commandresultcallback) {
        executioncontext.queueNext(new CommandQueueEntry<>(createTopFrame(executioncontext, commandresultcallback), (new CallFunction<>(instantiatedfunction, t0.callback(), false)).bind(t0)));
    }

    public static <T extends ExecutionCommandSource<T>> void queueInitialCommandExecution(ExecutionContext<T> executioncontext, String s, ContextChain<T> contextchain, T t0, CommandResultCallback commandresultcallback) {
        executioncontext.queueNext(new CommandQueueEntry<>(createTopFrame(executioncontext, commandresultcallback), new BuildContexts.b<>(s, contextchain, t0)));
    }

    private void handleQueueOverflow() {
        this.queueOverflow = true;
        this.newTopCommands.clear();
        this.commandQueue.clear();
    }

    public void queueNext(CommandQueueEntry<T> commandqueueentry) {
        if (this.newTopCommands.size() + this.commandQueue.size() > 10000000) {
            this.handleQueueOverflow();
        }

        if (!this.queueOverflow) {
            this.newTopCommands.add(commandqueueentry);
        }

    }

    public void discardAtDepthOrHigher(int i) {
        while (!this.commandQueue.isEmpty() && ((CommandQueueEntry) this.commandQueue.peek()).frame().depth() >= i) {
            this.commandQueue.removeFirst();
        }

    }

    public Frame.a frameControlForDepth(int i) {
        return () -> {
            this.discardAtDepthOrHigher(i);
        };
    }

    public void runCommandQueue() {
        this.pushNewCommands();

        while (true) {
            if (this.commandQuota <= 0) {
                ExecutionContext.LOGGER.info("Command execution stopped due to limit (executed {} commands)", this.commandLimit);
                break;
            }

            CommandQueueEntry<T> commandqueueentry = (CommandQueueEntry) this.commandQueue.pollFirst();

            if (commandqueueentry == null) {
                return;
            }

            this.currentFrameDepth = commandqueueentry.frame().depth();
            commandqueueentry.execute(this);
            if (this.queueOverflow) {
                ExecutionContext.LOGGER.error("Command execution stopped due to command queue overflow (max {})", 10000000);
                break;
            }

            this.pushNewCommands();
        }

        this.currentFrameDepth = 0;
    }

    private void pushNewCommands() {
        for (int i = this.newTopCommands.size() - 1; i >= 0; --i) {
            this.commandQueue.addFirst((CommandQueueEntry) this.newTopCommands.get(i));
        }

        this.newTopCommands.clear();
    }

    public void tracer(@Nullable TraceCallbacks tracecallbacks) {
        this.tracer = tracecallbacks;
    }

    @Nullable
    public TraceCallbacks tracer() {
        return this.tracer;
    }

    public GameProfilerFiller profiler() {
        return this.profiler;
    }

    public int forkLimit() {
        return this.forkLimit;
    }

    public void incrementCost() {
        --this.commandQuota;
    }

    public void close() {
        if (this.tracer != null) {
            this.tracer.close();
        }

    }
}
