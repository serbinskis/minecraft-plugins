package net.minecraft.commands.execution.tasks;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;

public class ExecuteCommand<T extends ExecutionCommandSource<T>> implements UnboundEntryAction<T> {

    private final String commandInput;
    private final ChainModifiers modifiers;
    private final CommandContext<T> executionContext;

    public ExecuteCommand(String s, ChainModifiers chainmodifiers, CommandContext<T> commandcontext) {
        this.commandInput = s;
        this.modifiers = chainmodifiers;
        this.executionContext = commandcontext;
    }

    public void execute(T t0, ExecutionContext<T> executioncontext, Frame frame) {
        executioncontext.profiler().push(() -> {
            return "execute " + this.commandInput;
        });

        try {
            executioncontext.incrementCost();
            int i = ContextChain.runExecutable(this.executionContext, t0, ExecutionCommandSource.resultConsumer(), this.modifiers.isForked());
            TraceCallbacks tracecallbacks = executioncontext.tracer();

            if (tracecallbacks != null) {
                tracecallbacks.onReturn(frame.depth(), this.commandInput, i);
            }
        } catch (CommandSyntaxException commandsyntaxexception) {
            t0.handleError(commandsyntaxexception, this.modifiers.isForked(), executioncontext.tracer());
        } finally {
            executioncontext.profiler().pop();
        }

    }
}
