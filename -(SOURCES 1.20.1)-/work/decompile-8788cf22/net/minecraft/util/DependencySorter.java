package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencySorter<K, V extends DependencySorter.a<K>> {

    private final Map<K, V> contents = new HashMap();

    public DependencySorter() {}

    public DependencySorter<K, V> addEntry(K k0, V v0) {
        this.contents.put(k0, v0);
        return this;
    }

    private void visitDependenciesAndElement(Multimap<K, K> multimap, Set<K> set, K k0, BiConsumer<K, V> biconsumer) {
        if (set.add(k0)) {
            multimap.get(k0).forEach((object) -> {
                this.visitDependenciesAndElement(multimap, set, object, biconsumer);
            });
            V v0 = (DependencySorter.a) this.contents.get(k0);

            if (v0 != null) {
                biconsumer.accept(k0, v0);
            }

        }
    }

    private static <K> boolean isCyclic(Multimap<K, K> multimap, K k0, K k1) {
        Collection<K> collection = multimap.get(k1);

        return collection.contains(k0) ? true : collection.stream().anyMatch((object) -> {
            return isCyclic(multimap, k0, object);
        });
    }

    private static <K> void addDependencyIfNotCyclic(Multimap<K, K> multimap, K k0, K k1) {
        if (!isCyclic(multimap, k0, k1)) {
            multimap.put(k0, k1);
        }

    }

    public void orderByDependencies(BiConsumer<K, V> biconsumer) {
        Multimap<K, K> multimap = HashMultimap.create();

        this.contents.forEach((object, dependencysorter_a) -> {
            dependencysorter_a.visitRequiredDependencies((object1) -> {
                addDependencyIfNotCyclic(multimap, object, object1);
            });
        });
        this.contents.forEach((object, dependencysorter_a) -> {
            dependencysorter_a.visitOptionalDependencies((object1) -> {
                addDependencyIfNotCyclic(multimap, object, object1);
            });
        });
        Set<K> set = new HashSet();

        this.contents.keySet().forEach((object) -> {
            this.visitDependenciesAndElement(multimap, set, object, biconsumer);
        });
    }

    public interface a<K> {

        void visitRequiredDependencies(Consumer<K> consumer);

        void visitOptionalDependencies(Consumer<K> consumer);
    }
}
