package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;

public class FutureChain implements TaskChainer, AutoCloseable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private CompletableFuture<?> head = CompletableFuture.completedFuture((Object) null);
    private final Executor executor;
    private volatile boolean closed;

    public FutureChain(Executor executor) {
        this.executor = executor;
    }

    @Override
    public <T> void append(CompletableFuture<T> completablefuture, Consumer<T> consumer) {
        this.head = this.head.thenCombine(completablefuture, (object, object1) -> {
            return object1;
        }).thenAcceptAsync((object) -> {
            if (!this.closed) {
                consumer.accept(object);
            }

        }, this.executor).exceptionally((throwable) -> {
            if (throwable instanceof CompletionException completionexception) {
                throwable = completionexception.getCause();
            }

            if (throwable instanceof CancellationException cancellationexception) {
                throw cancellationexception;
            } else {
                FutureChain.LOGGER.error("Chain link failed, continuing to next one", throwable);
                return null;
            }
        });
    }

    public void close() {
        this.closed = true;
    }
}
