package net.minecraft.server.level;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface ChunkResult<T> {

    static <T> ChunkResult<T> of(T t0) {
        return new ChunkResult.b<>(t0);
    }

    static <T> ChunkResult<T> error(String s) {
        return error(() -> {
            return s;
        });
    }

    static <T> ChunkResult<T> error(Supplier<String> supplier) {
        return new ChunkResult.a<>(supplier);
    }

    boolean isSuccess();

    @Nullable
    T orElse(@Nullable T t0);

    @Nullable
    static <R> R orElse(ChunkResult<? extends R> chunkresult, @Nullable R r0) {
        R r1 = chunkresult.orElse((Object) null);

        return r1 != null ? r1 : r0;
    }

    @Nullable
    String getError();

    ChunkResult<T> ifSuccess(Consumer<T> consumer);

    <R> ChunkResult<R> map(Function<T, R> function);

    <E extends Throwable> T orElseThrow(Supplier<E> supplier) throws E;

    public static record b<T>(T value) implements ChunkResult<T> {

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T orElse(@Nullable T t0) {
            return this.value;
        }

        @Nullable
        @Override
        public String getError() {
            return null;
        }

        @Override
        public ChunkResult<T> ifSuccess(Consumer<T> consumer) {
            consumer.accept(this.value);
            return this;
        }

        @Override
        public <R> ChunkResult<R> map(Function<T, R> function) {
            return new ChunkResult.b<>(function.apply(this.value));
        }

        @Override
        public <E extends Throwable> T orElseThrow(Supplier<E> supplier) throws E {
            return this.value;
        }
    }

    public static record a<T>(Supplier<String> error) implements ChunkResult<T> {

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Nullable
        @Override
        public T orElse(@Nullable T t0) {
            return t0;
        }

        @Override
        public String getError() {
            return (String) this.error.get();
        }

        @Override
        public ChunkResult<T> ifSuccess(Consumer<T> consumer) {
            return this;
        }

        @Override
        public <R> ChunkResult<R> map(Function<T, R> function) {
            return new ChunkResult.a<>(this.error);
        }

        @Override
        public <E extends Throwable> T orElseThrow(Supplier<E> supplier) throws E {
            throw (Throwable) supplier.get();
        }
    }
}
