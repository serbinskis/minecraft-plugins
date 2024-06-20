package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionCountsPredicate<T, P extends Predicate<T>> extends Predicate<Iterable<T>> {

    List<CollectionCountsPredicate.a<T, P>> unpack();

    static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate<T, P>> codec(Codec<P> codec) {
        return CollectionCountsPredicate.a.codec(codec).listOf().xmap(CollectionCountsPredicate::of, CollectionCountsPredicate::unpack);
    }

    @SafeVarargs
    static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(CollectionCountsPredicate.a<T, P>... acollectioncountspredicate_a) {
        return of(List.of(acollectioncountspredicate_a));
    }

    static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(List<CollectionCountsPredicate.a<T, P>> list) {
        Object object;

        switch (list.size()) {
            case 0:
                object = new CollectionCountsPredicate.d<>();
                break;
            case 1:
                object = new CollectionCountsPredicate.c<>((CollectionCountsPredicate.a) list.getFirst());
                break;
            default:
                object = new CollectionCountsPredicate.b<>(list);
        }

        return (CollectionCountsPredicate) object;
    }

    public static record a<T, P extends Predicate<T>>(P test, CriterionConditionValue.IntegerRange count) {

        public static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate.a<T, P>> codec(Codec<P> codec) {
            return RecordCodecBuilder.create((instance) -> {
                return instance.group(codec.fieldOf("test").forGetter(CollectionCountsPredicate.a::test), CriterionConditionValue.IntegerRange.CODEC.fieldOf("count").forGetter(CollectionCountsPredicate.a::count)).apply(instance, CollectionCountsPredicate.a::new);
            });
        }

        public boolean test(Iterable<T> iterable) {
            int i = 0;
            Iterator iterator = iterable.iterator();

            while (iterator.hasNext()) {
                T t0 = iterator.next();

                if (this.test.test(t0)) {
                    ++i;
                }
            }

            return this.count.matches(i);
        }
    }

    public static class d<T, P extends Predicate<T>> implements CollectionCountsPredicate<T, P> {

        public d() {}

        public boolean test(Iterable<T> iterable) {
            return true;
        }

        @Override
        public List<CollectionCountsPredicate.a<T, P>> unpack() {
            return List.of();
        }
    }

    public static record c<T, P extends Predicate<T>>(CollectionCountsPredicate.a<T, P> entry) implements CollectionCountsPredicate<T, P> {

        public boolean test(Iterable<T> iterable) {
            return this.entry.test(iterable);
        }

        @Override
        public List<CollectionCountsPredicate.a<T, P>> unpack() {
            return List.of(this.entry);
        }
    }

    public static record b<T, P extends Predicate<T>>(List<CollectionCountsPredicate.a<T, P>> entries) implements CollectionCountsPredicate<T, P> {

        public boolean test(Iterable<T> iterable) {
            Iterator iterator = this.entries.iterator();

            CollectionCountsPredicate.a collectioncountspredicate_a;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                collectioncountspredicate_a = (CollectionCountsPredicate.a) iterator.next();
            } while (collectioncountspredicate_a.test(iterable));

            return false;
        }

        @Override
        public List<CollectionCountsPredicate.a<T, P>> unpack() {
            return this.entries;
        }
    }
}
