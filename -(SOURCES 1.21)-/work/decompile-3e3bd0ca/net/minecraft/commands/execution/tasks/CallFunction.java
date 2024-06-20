package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.InstantiatedFunction;

public class CallFunction<T extends ExecutionCommandSource<T>> implements UnboundEntryAction<T> {

    private final InstantiatedFunction<T> function;
    private final CommandResultCallback resultCallback;
    private final boolean returnParentFrame;

    public CallFunction(InstantiatedFunction<T> instantiatedfunction, CommandResultCallback commandresultcallback, boolean flag) {
        this.function = instantiatedfunction;
        this.resultCallback = commandresultcallback;
        this.returnParentFrame = flag;
    }

    public void execute(T t0, ExecutionContext<T> executioncontext, Frame frame) {
        executioncontext.incrementCost();
        List<UnboundEntryAction<T>> list = this.function.entries();
        TraceCallbacks tracecallbacks = executioncontext.tracer();

        if (tracecallbacks != null) {
            tracecallbacks.onCall(frame.depth(), this.function.id(), this.function.entries().size());
        }

        int i = frame.depth() + 1;
        Frame.a frame_a = this.returnParentFrame ? frame.frameControl() : executioncontext.frameControlForDepth(i);
        Frame frame1 = new Frame(i, this.resultCallback, frame_a);

        ContinuationTask.schedule(executioncontext, frame1, list, (frame2, unboundentryaction) -> {
            return new CommandQueueEntry<>(frame2, unboundentryaction.bind(t0));
        });
    }
}
