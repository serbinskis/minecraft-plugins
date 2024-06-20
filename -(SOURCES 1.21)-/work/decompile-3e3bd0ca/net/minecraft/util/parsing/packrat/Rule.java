package net.minecraft.util.parsing.packrat;

import java.util.Optional;

public interface Rule<S, T> {

    Optional<T> parse(ParseState<S> parsestate);

    static <S, T> Rule<S, T> fromTerm(Term<S> term, Rule.a<S, T> rule_a) {
        return new Rule.c<>(rule_a, term);
    }

    static <S, T> Rule<S, T> fromTerm(Term<S> term, Rule.b<T> rule_b) {
        return new Rule.c<>((parsestate, scope) -> {
            return Optional.of(rule_b.run(scope));
        }, term);
    }

    public static record c<S, T>(Rule.a<S, T> action, Term<S> child) implements Rule<S, T> {

        @Override
        public Optional<T> parse(ParseState<S> parsestate) {
            Scope scope = new Scope();

            return this.child.parse(parsestate, scope, Control.UNBOUND) ? this.action.run(parsestate, scope) : Optional.empty();
        }
    }

    @FunctionalInterface
    public interface a<S, T> {

        Optional<T> run(ParseState<S> parsestate, Scope scope);
    }

    @FunctionalInterface
    public interface b<T> {

        T run(Scope scope);
    }
}
