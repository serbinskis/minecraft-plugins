package net.minecraft.util.parsing.packrat;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Objects;
import javax.annotation.Nullable;

public final class Scope {

    private final Object2ObjectMap<Atom<?>, Object> values = new Object2ObjectArrayMap();

    public Scope() {}

    public <T> void put(Atom<T> atom, @Nullable T t0) {
        this.values.put(atom, t0);
    }

    @Nullable
    public <T> T get(Atom<T> atom) {
        return this.values.get(atom);
    }

    public <T> T getOrThrow(Atom<T> atom) {
        return Objects.requireNonNull(this.get(atom));
    }

    public <T> T getOrDefault(Atom<T> atom, T t0) {
        return Objects.requireNonNullElse(this.get(atom), t0);
    }

    @Nullable
    @SafeVarargs
    public final <T> T getAny(Atom<T>... aatom) {
        Atom[] aatom1 = aatom;
        int i = aatom.length;

        for (int j = 0; j < i; ++j) {
            Atom<T> atom = aatom1[j];
            T t0 = this.get(atom);

            if (t0 != null) {
                return t0;
            }
        }

        return null;
    }

    @SafeVarargs
    public final <T> T getAnyOrThrow(Atom<T>... aatom) {
        return Objects.requireNonNull(this.getAny(aatom));
    }

    public String toString() {
        return this.values.toString();
    }

    public void putAll(Scope scope) {
        this.values.putAll(scope.values);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof Scope) {
            Scope scope = (Scope) object;

            return this.values.equals(scope.values);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.values.hashCode();
    }
}
