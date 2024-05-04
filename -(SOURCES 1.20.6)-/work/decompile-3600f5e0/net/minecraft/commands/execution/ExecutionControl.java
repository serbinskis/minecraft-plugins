package net.minecraft.commands.execution;

import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;

public interface ExecutionControl<T> {

    void queueNext(EntryAction<T> entryaction);

    void tracer(@Nullable TraceCallbacks tracecallbacks);

    @Nullable
    TraceCallbacks tracer();

    Frame currentFrame();

    static <T extends ExecutionCommandSource<T>> ExecutionControl<T> create(final ExecutionContext<T> executioncontext, final Frame frame) {
        return new ExecutionControl<T>() {
            @Override
            public void queueNext(EntryAction<T> entryaction) {
                executioncontext.queueNext(new CommandQueueEntry<>(frame, entryaction));
            }

            @Override
            public void tracer(@Nullable TraceCallbacks tracecallbacks) {
                executioncontext.tracer(tracecallbacks);
            }

            @Nullable
            @Override
            public TraceCallbacks tracer() {
                return executioncontext.tracer();
            }

            @Override
            public Frame currentFrame() {
                return frame;
            }
        };
    }
}
