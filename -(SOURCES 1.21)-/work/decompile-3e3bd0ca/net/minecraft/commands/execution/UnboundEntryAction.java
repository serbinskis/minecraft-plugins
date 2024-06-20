package net.minecraft.commands.execution;

@FunctionalInterface
public interface UnboundEntryAction<T> {

    void execute(T t0, ExecutionContext<T> executioncontext, Frame frame);

    default EntryAction<T> bind(T t0) {
        return (executioncontext, frame) -> {
            this.execute(t0, executioncontext, frame);
        };
    }
}
