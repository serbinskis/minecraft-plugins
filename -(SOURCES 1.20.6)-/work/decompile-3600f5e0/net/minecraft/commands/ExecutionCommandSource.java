package net.minecraft.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.execution.TraceCallbacks;

public interface ExecutionCommandSource<T extends ExecutionCommandSource<T>> {

    boolean hasPermission(int i);

    T withCallback(CommandResultCallback commandresultcallback);

    CommandResultCallback callback();

    default T clearCallbacks() {
        return this.withCallback(CommandResultCallback.EMPTY);
    }

    com.mojang.brigadier.CommandDispatcher<T> dispatcher();

    void handleError(CommandExceptionType commandexceptiontype, Message message, boolean flag, @Nullable TraceCallbacks tracecallbacks);

    boolean isSilent();

    default void handleError(CommandSyntaxException commandsyntaxexception, boolean flag, @Nullable TraceCallbacks tracecallbacks) {
        this.handleError(commandsyntaxexception.getType(), commandsyntaxexception.getRawMessage(), flag, tracecallbacks);
    }

    static <T extends ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
        return (commandcontext, flag, i) -> {
            ((ExecutionCommandSource) commandcontext.getSource()).callback().onResult(flag, i);
        };
    }
}
