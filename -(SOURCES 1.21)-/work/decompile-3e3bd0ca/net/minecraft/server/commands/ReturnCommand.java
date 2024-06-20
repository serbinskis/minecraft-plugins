package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ContextChain;
import java.util.List;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.FallthroughTask;

public class ReturnCommand {

    public ReturnCommand() {}

    public static <T extends ExecutionCommandSource<T>> void register(CommandDispatcher<T> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("return").requires((executioncommandsource) -> {
            return executioncommandsource.hasPermission(2);
        })).then(RequiredArgumentBuilder.argument("value", IntegerArgumentType.integer()).executes(new ReturnCommand.c<>()))).then(LiteralArgumentBuilder.literal("fail").executes(new ReturnCommand.a<>()))).then(LiteralArgumentBuilder.literal("run").forward(commanddispatcher.getRoot(), new ReturnCommand.b<>(), false)));
    }

    private static class c<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor.a<T> {

        c() {}

        public void run(T t0, ContextChain<T> contextchain, ChainModifiers chainmodifiers, ExecutionControl<T> executioncontrol) {
            int i = IntegerArgumentType.getInteger(contextchain.getTopContext(), "value");

            t0.callback().onSuccess(i);
            Frame frame = executioncontrol.currentFrame();

            frame.returnSuccess(i);
            frame.discard();
        }
    }

    private static class a<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor.a<T> {

        a() {}

        public void run(T t0, ContextChain<T> contextchain, ChainModifiers chainmodifiers, ExecutionControl<T> executioncontrol) {
            t0.callback().onFailure();
            Frame frame = executioncontrol.currentFrame();

            frame.returnFailure();
            frame.discard();
        }
    }

    private static class b<T extends ExecutionCommandSource<T>> implements CustomModifierExecutor.a<T> {

        b() {}

        public void apply(T t0, List<T> list, ContextChain<T> contextchain, ChainModifiers chainmodifiers, ExecutionControl<T> executioncontrol) {
            if (list.isEmpty()) {
                if (chainmodifiers.isReturn()) {
                    executioncontrol.queueNext(FallthroughTask.instance());
                }

            } else {
                executioncontrol.currentFrame().discard();
                ContextChain<T> contextchain1 = contextchain.nextStage();
                String s = contextchain1.getTopContext().getInput();

                executioncontrol.queueNext(new BuildContexts.a<>(s, contextchain1, chainmodifiers.setReturn(), t0, list));
            }
        }
    }
}
