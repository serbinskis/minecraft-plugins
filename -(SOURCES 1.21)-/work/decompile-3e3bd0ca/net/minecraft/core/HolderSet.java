package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.VisibleForTesting;

public interface HolderSet<T> extends Iterable<Holder<T>> {

    Stream<Holder<T>> stream();

    int size();

    Either<TagKey<T>, List<Holder<T>>> unwrap();

    Optional<Holder<T>> getRandomElement(RandomSource randomsource);

    Holder<T> get(int i);

    boolean contains(Holder<T> holder);

    boolean canSerializeIn(HolderOwner<T> holderowner);

    Optional<TagKey<T>> unwrapKey();

    /** @deprecated */
    @Deprecated
    @VisibleForTesting
    static <T> HolderSet.Named<T> emptyNamed(HolderOwner<T> holderowner, TagKey<T> tagkey) {
        return new HolderSet.Named<T>(holderowner, tagkey) {
            @Override
            protected List<Holder<T>> contents() {
                throw new UnsupportedOperationException("Tag " + String.valueOf(this.key()) + " can't be dereferenced during construction");
            }
        };
    }

    static <T> HolderSet<T> empty() {
        return HolderSet.a.EMPTY;
    }

    @SafeVarargs
    static <T> HolderSet.a<T> direct(Holder<T>... aholder) {
        return new HolderSet.a<>(List.of(aholder));
    }

    static <T> HolderSet.a<T> direct(List<? extends Holder<T>> list) {
        return new HolderSet.a<>(List.copyOf(list));
    }

    @SafeVarargs
    static <E, T> HolderSet.a<T> direct(Function<E, Holder<T>> function, E... ae) {
        return direct(Stream.of(ae).map(function).toList());
    }

    static <E, T> HolderSet.a<T> direct(Function<E, Holder<T>> function, Collection<E> collection) {
        return direct(collection.stream().map(function).toList());
    }

    public static final class a<T> extends HolderSet.b<T> {

        static final HolderSet.a<?> EMPTY = new HolderSet.a<>(List.of());
        private final List<Holder<T>> contents;
        @Nullable
        private Set<Holder<T>> contentsSet;

        a(List<Holder<T>> list) {
            this.contents = list;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.contents;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.right(this.contents);
        }

        @Override
        public Optional<TagKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public boolean contains(Holder<T> holder) {
            if (this.contentsSet == null) {
                this.contentsSet = Set.copyOf(this.contents);
            }

            return this.contentsSet.contains(holder);
        }

        public String toString() {
            return "DirectSet[" + String.valueOf(this.contents) + "]";
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else {
                boolean flag;

                if (object instanceof HolderSet.a) {
                    HolderSet.a<?> holderset_a = (HolderSet.a) object;

                    if (this.contents.equals(holderset_a.contents)) {
                        flag = true;
                        return flag;
                    }
                }

                flag = false;
                return flag;
            }
        }

        public int hashCode() {
            return this.contents.hashCode();
        }
    }

    public static class Named<T> extends HolderSet.b<T> {

        private final HolderOwner<T> owner;
        private final TagKey<T> key;
        private List<Holder<T>> contents = List.of();

        Named(HolderOwner<T> holderowner, TagKey<T> tagkey) {
            this.owner = holderowner;
            this.key = tagkey;
        }

        void bind(List<Holder<T>> list) {
            this.contents = List.copyOf(list);
        }

        public TagKey<T> key() {
            return this.key;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.contents;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public Optional<TagKey<T>> unwrapKey() {
            return Optional.of(this.key);
        }

        @Override
        public boolean contains(Holder<T> holder) {
            return holder.is(this.key);
        }

        public String toString() {
            String s = String.valueOf(this.key);

            return "NamedSet(" + s + ")[" + String.valueOf(this.contents) + "]";
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderowner) {
            return this.owner.canSerializeIn(holderowner);
        }
    }

    public abstract static class b<T> implements HolderSet<T> {

        public b() {}

        protected abstract List<Holder<T>> contents();

        @Override
        public int size() {
            return this.contents().size();
        }

        public Spliterator<Holder<T>> spliterator() {
            return this.contents().spliterator();
        }

        public Iterator<Holder<T>> iterator() {
            return this.contents().iterator();
        }

        @Override
        public Stream<Holder<T>> stream() {
            return this.contents().stream();
        }

        @Override
        public Optional<Holder<T>> getRandomElement(RandomSource randomsource) {
            return SystemUtils.getRandomSafe(this.contents(), randomsource);
        }

        @Override
        public Holder<T> get(int i) {
            return (Holder) this.contents().get(i);
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderowner) {
            return true;
        }
    }
}
