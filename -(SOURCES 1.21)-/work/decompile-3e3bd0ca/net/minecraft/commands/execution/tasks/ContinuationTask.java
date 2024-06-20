package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

public class ContinuationTask<T, P> implements EntryAction<T> {

    private final ContinuationTask.a<T, P> taskFactory;
    private final List<P> arguments;
    private final CommandQueueEntry<T> selfEntry;
    private int index;

    private ContinuationTask(ContinuationTask.a<T, P> continuationtask_a, List<P> list, Frame frame) {
        this.taskFactory = continuationtask_a;
        this.arguments = list;
        this.selfEntry = new CommandQueueEntry<>(frame, this);
    }

    @Override
    public void execute(ExecutionContext<T> executioncontext, Frame frame) {
        P p0 = this.arguments.get(this.index);

        executioncontext.queueNext(this.taskFactory.create(frame, p0));
        if (++this.index < this.arguments.size()) {
            executioncontext.queueNext(this.selfEntry);
        }

    }

    public static <T, P> void schedule(ExecutionContext<T> executioncontext, Frame frame, List<P> list, ContinuationTask.a<T, P> continuationtask_a) {
        int i = list.size();

        switch (i) {
            case 0:
                break;
            case 1:
                executioncontext.queueNext(continuationtask_a.create(frame, list.get(0)));
                break;
            case 2:
                executioncontext.queueNext(continuationtask_a.create(frame, list.get(0)));
                executioncontext.queueNext(continuationtask_a.create(frame, list.get(1)));
                break;
            default:
                executioncontext.queueNext((new ContinuationTask<>(continuationtask_a, list, frame)).selfEntry);
        }

    }

    @FunctionalInterface
    public interface a<T, P> {

        CommandQueueEntry<T> create(Frame frame, P p0);
    }
}
