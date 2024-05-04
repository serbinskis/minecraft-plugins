package net.minecraft.commands.execution.tasks;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.context.ContextChain.Stage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.network.chat.IChatBaseComponent;

public class BuildContexts<T extends ExecutionCommandSource<T>> {

    @VisibleForTesting
    public static final DynamicCommandExceptionType ERROR_FORK_LIMIT_REACHED = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("command.forkLimit", object);
    });
    private final String commandInput;
    private final ContextChain<T> command;

    public BuildContexts(String s, ContextChain<T> contextchain) {
        this.commandInput = s;
        this.command = contextchain;
    }

    protected void execute(T t0, List<T> list, ExecutionContext<T> executioncontext, Frame frame, ChainModifiers chainmodifiers) {
        ContextChain<T> contextchain = this.command;
        ChainModifiers chainmodifiers1 = chainmodifiers;
        List<T> list1 = list;

        if (contextchain.getStage() != Stage.EXECUTE) {
            executioncontext.profiler().push(() -> {
                return "prepare " + this.commandInput;
            });

            try {
                for (int i = executioncontext.forkLimit(); contextchain.getStage() != Stage.EXECUTE; contextchain = contextchain.nextStage()) {
                    CommandContext<T> commandcontext = contextchain.getTopContext();

                    if (commandcontext.isForked()) {
                        chainmodifiers1 = chainmodifiers1.setForked();
                    }

                    RedirectModifier<T> redirectmodifier = commandcontext.getRedirectModifier();

                    if (redirectmodifier instanceof CustomModifierExecutor) {
                        CustomModifierExecutor<T> custommodifierexecutor = (CustomModifierExecutor) redirectmodifier;

                        custommodifierexecutor.apply(t0, (List) list1, contextchain, chainmodifiers1, ExecutionControl.create(executioncontext, frame));
                        return;
                    }

                    if (redirectmodifier != null) {
                        executioncontext.incrementCost();
                        boolean flag = chainmodifiers1.isForked();
                        List<T> list2 = new ObjectArrayList();
                        Iterator iterator = ((List) list1).iterator();

                        while (iterator.hasNext()) {
                            T t1 = (ExecutionCommandSource) iterator.next();

                            try {
                                Collection<T> collection = ContextChain.runModifier(commandcontext, t1, (commandcontext1, flag1, j) -> {
                                }, flag);

                                if (list2.size() + collection.size() >= i) {
                                    t0.handleError(BuildContexts.ERROR_FORK_LIMIT_REACHED.create(i), flag, executioncontext.tracer());
                                    return;
                                }

                                list2.addAll(collection);
                            } catch (CommandSyntaxException commandsyntaxexception) {
                                t1.handleError(commandsyntaxexception, flag, executioncontext.tracer());
                                if (!flag) {
                                    return;
                                }
                            }
                        }

                        list1 = list2;
                    }
                }
            } finally {
                executioncontext.profiler().pop();
            }
        }

        if (((List) list1).isEmpty()) {
            if (chainmodifiers1.isReturn()) {
                executioncontext.queueNext(new CommandQueueEntry<>(frame, FallthroughTask.instance()));
            }

        } else {
            CommandContext<T> commandcontext1 = contextchain.getTopContext();
            Command<T> command = commandcontext1.getCommand();

            if (command instanceof CustomCommandExecutor) {
                CustomCommandExecutor<T> customcommandexecutor = (CustomCommandExecutor) command;
                ExecutionControl<T> executioncontrol = ExecutionControl.create(executioncontext, frame);
                Iterator iterator1 = ((List) list1).iterator();

                while (iterator1.hasNext()) {
                    T t2 = (ExecutionCommandSource) iterator1.next();

                    customcommandexecutor.run(t2, contextchain, chainmodifiers1, executioncontrol);
                }
            } else {
                if (chainmodifiers1.isReturn()) {
                    T t3 = (ExecutionCommandSource) ((List) list1).get(0);

                    t3 = t3.withCallback(CommandResultCallback.chain(t3.callback(), frame.returnValueConsumer()));
                    list1 = List.of(t3);
                }

                ExecuteCommand<T> executecommand = new ExecuteCommand<>(this.commandInput, chainmodifiers1, commandcontext1);

                ContinuationTask.schedule(executioncontext, frame, (List) list1, (frame1, executioncommandsource) -> {
                    return new CommandQueueEntry<>(frame1, executecommand.bind(executioncommandsource));
                });
            }

        }
    }

    protected void traceCommandStart(ExecutionContext<T> executioncontext, Frame frame) {
        TraceCallbacks tracecallbacks = executioncontext.tracer();

        if (tracecallbacks != null) {
            tracecallbacks.onCommand(frame.depth(), this.commandInput);
        }

    }

    public String toString() {
        return this.commandInput;
    }

    public static class b<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {

        private final T source;

        public b(String s, ContextChain<T> contextchain, T t0) {
            super(s, contextchain);
            this.source = t0;
        }

        @Override
        public void execute(ExecutionContext<T> executioncontext, Frame frame) {
            this.traceCommandStart(executioncontext, frame);
            this.execute(this.source, List.of(this.source), executioncontext, frame, ChainModifiers.DEFAULT);
        }
    }

    public static class a<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {

        private final ChainModifiers modifiers;
        private final T originalSource;
        private final List<T> sources;

        public a(String s, ContextChain<T> contextchain, ChainModifiers chainmodifiers, T t0, List<T> list) {
            super(s, contextchain);
            this.originalSource = t0;
            this.sources = list;
            this.modifiers = chainmodifiers;
        }

        @Override
        public void execute(ExecutionContext<T> executioncontext, Frame frame) {
            this.execute(this.originalSource, this.sources, executioncontext, frame, this.modifiers);
        }
    }

    public static class c<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements UnboundEntryAction<T> {

        public c(String s, ContextChain<T> contextchain) {
            super(s, contextchain);
        }

        public void execute(T t0, ExecutionContext<T> executioncontext, Frame frame) {
            this.traceCommandStart(executioncontext, frame);
            this.execute(t0, List.of(t0), executioncontext, frame, ChainModifiers.DEFAULT);
        }
    }
}
