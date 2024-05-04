package net.minecraft.util.parsing.packrat;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class Dictionary<S> {

    private final Map<Atom<?>, Rule<S, ?>> terms = new HashMap();

    public Dictionary() {}

    public <T> void put(Atom<T> atom, Rule<S, T> rule) {
        Rule<S, ?> rule1 = (Rule) this.terms.putIfAbsent(atom, rule);

        if (rule1 != null) {
            throw new IllegalArgumentException("Trying to override rule: " + String.valueOf(atom));
        }
    }

    public <T> void put(Atom<T> atom, Term<S> term, Rule.a<S, T> rule_a) {
        this.put(atom, Rule.fromTerm(term, rule_a));
    }

    public <T> void put(Atom<T> atom, Term<S> term, Rule.b<T> rule_b) {
        this.put(atom, Rule.fromTerm(term, rule_b));
    }

    @Nullable
    public <T> Rule<S, T> get(Atom<T> atom) {
        return (Rule) this.terms.get(atom);
    }
}
