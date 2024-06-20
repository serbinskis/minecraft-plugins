package net.minecraft.util.parsing.packrat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public abstract class ParseState<S> {

    private final Map<ParseState.b<?>, ParseState.a<?>> ruleCache = new HashMap();
    private final Dictionary<S> dictionary;
    private final ErrorCollector<S> errorCollector;

    protected ParseState(Dictionary<S> dictionary, ErrorCollector<S> errorcollector) {
        this.dictionary = dictionary;
        this.errorCollector = errorcollector;
    }

    public ErrorCollector<S> errorCollector() {
        return this.errorCollector;
    }

    public <T> Optional<T> parseTopRule(Atom<T> atom) {
        Optional<T> optional = this.parse(atom);

        if (optional.isPresent()) {
            this.errorCollector.finish(this.mark());
        }

        return optional;
    }

    public <T> Optional<T> parse(Atom<T> atom) {
        ParseState.b<T> parsestate_b = new ParseState.b<>(atom, this.mark());
        ParseState.a<T> parsestate_a = this.lookupInCache(parsestate_b);

        if (parsestate_a != null) {
            this.restore(parsestate_a.mark());
            return parsestate_a.value;
        } else {
            Rule<S, T> rule = this.dictionary.get(atom);

            if (rule == null) {
                throw new IllegalStateException("No symbol " + String.valueOf(atom));
            } else {
                Optional<T> optional = rule.parse(this);

                this.storeInCache(parsestate_b, optional);
                return optional;
            }
        }
    }

    @Nullable
    private <T> ParseState.a<T> lookupInCache(ParseState.b<T> parsestate_b) {
        return (ParseState.a) this.ruleCache.get(parsestate_b);
    }

    private <T> void storeInCache(ParseState.b<T> parsestate_b, Optional<T> optional) {
        this.ruleCache.put(parsestate_b, new ParseState.a<>(optional, this.mark()));
    }

    public abstract S input();

    public abstract int mark();

    public abstract void restore(int i);

    private static record b<T>(Atom<T> name, int mark) {

    }

    private static record a<T>(Optional<T> value, int mark) {

    }
}
