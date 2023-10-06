package net.minecraft.world.level.storage.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;

public class LootCollector {

    private final Multimap<String, String> problems;
    private final Supplier<String> context;
    private final LootContextParameterSet params;
    private final LootDataResolver resolver;
    private final Set<LootDataId<?>> visitedElements;
    @Nullable
    private String contextCache;

    public LootCollector(LootContextParameterSet lootcontextparameterset, LootDataResolver lootdataresolver) {
        this(HashMultimap.create(), () -> {
            return "";
        }, lootcontextparameterset, lootdataresolver, ImmutableSet.of());
    }

    public LootCollector(Multimap<String, String> multimap, Supplier<String> supplier, LootContextParameterSet lootcontextparameterset, LootDataResolver lootdataresolver, Set<LootDataId<?>> set) {
        this.problems = multimap;
        this.context = supplier;
        this.params = lootcontextparameterset;
        this.resolver = lootdataresolver;
        this.visitedElements = set;
    }

    private String getContext() {
        if (this.contextCache == null) {
            this.contextCache = (String) this.context.get();
        }

        return this.contextCache;
    }

    public void reportProblem(String s) {
        this.problems.put(this.getContext(), s);
    }

    public LootCollector forChild(String s) {
        return new LootCollector(this.problems, () -> {
            String s1 = this.getContext();

            return s1 + s;
        }, this.params, this.resolver, this.visitedElements);
    }

    public LootCollector enterElement(String s, LootDataId<?> lootdataid) {
        ImmutableSet<LootDataId<?>> immutableset = ImmutableSet.builder().addAll(this.visitedElements).add(lootdataid).build();

        return new LootCollector(this.problems, () -> {
            String s1 = this.getContext();

            return s1 + s;
        }, this.params, this.resolver, immutableset);
    }

    public boolean hasVisitedElement(LootDataId<?> lootdataid) {
        return this.visitedElements.contains(lootdataid);
    }

    public Multimap<String, String> getProblems() {
        return ImmutableMultimap.copyOf(this.problems);
    }

    public void validateUser(LootItemUser lootitemuser) {
        this.params.validateUser(this, lootitemuser);
    }

    public LootDataResolver resolver() {
        return this.resolver;
    }

    public LootCollector setParams(LootContextParameterSet lootcontextparameterset) {
        return new LootCollector(this.problems, this.context, lootcontextparameterset, this.resolver, this.visitedElements);
    }
}
