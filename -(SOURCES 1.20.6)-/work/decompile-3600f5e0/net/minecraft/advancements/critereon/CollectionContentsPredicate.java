package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionContentsPredicate<T, P extends Predicate<T>> extends Predicate<Iterable<T>> {

    List<P> unpack();

    static <T, P extends Predicate<T>> Codec<CollectionContentsPredicate<T, P>> codec(Codec<P> codec) {
        return codec.listOf().xmap(CollectionContentsPredicate::of, CollectionContentsPredicate::unpack);
    }

    @SafeVarargs
    static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(P... ap) {
        return of(List.of(ap));
    }

    static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(List<P> list) {
        Object object;

        switch (list.size()) {
            case 0:
                object = new CollectionContentsPredicate.c<>();
                break;
            case 1:
                object = new CollectionContentsPredicate.b<>((Predicate) list.getFirst());
                break;
            default:
                object = new CollectionContentsPredicate.a<>(list);
        }

        return (CollectionContentsPredicate) object;
    }

    public static class c<T, P extends Predicate<T>> implements CollectionContentsPredicate<T, P> {

        public c() {}

        public boolean test(Iterable<T> iterable) {
            return true;
        }

        @Override
        public List<P> unpack() {
            return List.of();
        }
    }

    public static record b<T, P extends Predicate<T>>(P test) implements CollectionContentsPredicate<T, P> {

        public boolean test(Iterable<T> iterable) {
            Iterator iterator = iterable.iterator();

            Object object;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                object = iterator.next();
            } while (!this.test.test(object));

            return true;
        }

        @Override
        public List<P> unpack() {
            return List.of(this.test);
        }
    }

    public static record a<T, P extends Predicate<T>>(List<P> tests) implements CollectionContentsPredicate<T, P> {

        public boolean test(Iterable<T> iterable) {
            List<Predicate<T>> list = new ArrayList(this.tests);
            Iterator iterator = iterable.iterator();

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                T t0 = iterator.next();

                list.removeIf((predicate) -> {
                    return predicate.test(t0);
                });
            } while (!list.isEmpty());

            return true;
        }

        @Override
        public List<P> unpack() {
            return this.tests;
        }
    }
}
