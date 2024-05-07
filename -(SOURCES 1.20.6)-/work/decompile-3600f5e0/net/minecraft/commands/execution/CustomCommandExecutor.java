package net.minecraft.commands.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;

public interface CustomCommandExecutor<T> {

    void run(T t0, ContextChain<T> contextchain, ChainModifiers chainmodifiers, ExecutionControl<T> executioncontrol);

    public abstract static class b<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor<T> {

        public b() {}

        public final void run(T t0, ContextChain<T> contextchain, ChainModifiers chainmodifiers, ExecutionControl<T> executioncontrol) {
            try {
                this.runGuarded(t0, contextchain, chainmodifiers, executioncontrol);
            } catch (CommandSyntaxException commandsyntaxexception) {
                this.onError(commandsyntaxexception, t0, chainmodifiers, executioncontrol.tracer());
                t0.callback().onFailure();
            }

        }

        protected void onError(CommandSyntaxException commandsyntaxexception, T t0, ChainModifiers chainmodifiers, @Nullable TraceCallbacks tracecallbacks) {
            t0.handleError(commandsyntaxexception, chainmodifiers.isForked(), tracecallbacks);
        }

        protected abstract void runGuarded(T t0, ContextChain<T> contextchain, ChainModifiers chainmodifiers, ExecutionControl<T> executioncontrol) throws CommandSyntaxException;
    }

    public interface a<T> extends Command<T>, CustomCommandExecutor<T> {

        default int run(CommandContext<T> commandcontext) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}
