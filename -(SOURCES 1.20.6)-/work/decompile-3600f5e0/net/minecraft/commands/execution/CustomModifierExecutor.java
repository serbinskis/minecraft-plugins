package net.minecraft.commands.execution;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;

public interface CustomModifierExecutor<T> {

    void apply(T t0, List<T> list, ContextChain<T> contextchain, ChainModifiers chainmodifiers, ExecutionControl<T> executioncontrol);

    public interface a<T> extends RedirectModifier<T>, CustomModifierExecutor<T> {

        default Collection<T> apply(CommandContext<T> commandcontext) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}
