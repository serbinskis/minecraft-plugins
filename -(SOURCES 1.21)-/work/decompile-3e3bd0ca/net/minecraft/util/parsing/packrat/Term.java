package net.minecraft.util.parsing.packrat;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.mutable.MutableBoolean;

public interface Term<S> {

    boolean parse(ParseState<S> parsestate, Scope scope, Control control);

    static <S> Term<S> named(Atom<?> atom) {
        return new Term.d<>(atom);
    }

    static <S, T> Term<S> marker(Atom<T> atom, T t0) {
        return new Term.b<>(atom, t0);
    }

    @SafeVarargs
    static <S> Term<S> sequence(Term<S>... aterm) {
        return new Term.e<>(List.of(aterm));
    }

    @SafeVarargs
    static <S> Term<S> alternative(Term<S>... aterm) {
        return new Term.a<>(List.of(aterm));
    }

    static <S> Term<S> optional(Term<S> term) {
        return new Term.c<>(term);
    }

    static <S> Term<S> cut() {
        return new Term<S>() {
            @Override
            public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
                control.cut();
                return true;
            }

            public String toString() {
                return "\u2191";
            }
        };
    }

    static <S> Term<S> empty() {
        return new Term<S>() {
            @Override
            public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
                return true;
            }

            public String toString() {
                return "\u03b5";
            }
        };
    }

    public static record d<S, T>(Atom<T> name) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            Optional<T> optional = parsestate.parse(this.name);

            if (optional.isEmpty()) {
                return false;
            } else {
                scope.put(this.name, optional.get());
                return true;
            }
        }
    }

    public static record b<S, T>(Atom<T> name, T value) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            scope.put(this.name, this.value);
            return true;
        }
    }

    public static record e<S>(List<Term<S>> elements) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            int i = parsestate.mark();
            Iterator iterator = this.elements.iterator();

            Term term;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                term = (Term) iterator.next();
            } while (term.parse(parsestate, scope, control));

            parsestate.restore(i);
            return false;
        }
    }

    public static record a<S>(List<Term<S>> elements) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            MutableBoolean mutableboolean = new MutableBoolean();

            Objects.requireNonNull(mutableboolean);
            Control control1 = mutableboolean::setTrue;
            int i = parsestate.mark();
            Iterator iterator = this.elements.iterator();

            while (iterator.hasNext()) {
                Term<S> term = (Term) iterator.next();

                if (mutableboolean.isTrue()) {
                    break;
                }

                Scope scope1 = new Scope();

                if (term.parse(parsestate, scope1, control1)) {
                    scope.putAll(scope1);
                    return true;
                }

                parsestate.restore(i);
            }

            return false;
        }
    }

    public static record c<S>(Term<S> term) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            int i = parsestate.mark();

            if (!this.term.parse(parsestate, scope, control)) {
                parsestate.restore(i);
            }

            return true;
        }
    }
}
